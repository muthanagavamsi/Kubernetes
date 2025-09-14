Source: https://medium.com/@martin.hodges/using-kind-to-develop-and-test-your-kubernetes-deployments-54093692c9fa

Using Kind to develop and test your Kubernetes deployments
In a previous article, I provided a project to set up a Kubernetes cluster in the cloud. The downside of this is that it incurs costs. If you are sensitive to costs of any type, you may want a local implementation of that project to develop and test your applications. Here I introduce Kind to do just that.
Martin Hodges
Martin Hodges

Kind architecture
Local development
Spinning up a Kubernetes cluster in the cloud requires time and money. Time to spin it up and money to pay the bill!

As a developer, I want to be able to spin up a cluster that looks like the cluster in production and use this to test my application and, just as importantly, the deployment of my application. I want to do this quickly and at little to no cost.

I need a local cluster.

There are several options to do this, including, K3d, K3s, Kind, Microk8s and Minikube.

Minikube is the most popular but lacks the multi-node capability I want. Kind is a lightweight application that supports multiple nodes and is the tool I have chosen for this article. Others have more complex setup.

Installation
You can find installation instructions for most operating systems here.

As I am using a Mac and so I will be showing you the commands based on a macOS environment. I am also going to assume you have homebrew and Docker installed.

You can install Kind with:

brew install kind
You can check your version with:

kind version
This gives me:

kind v0.22.0 go1.21.7 darwin/arm64

Installation done.

Creating a cluster
Now, to create a cluster, use:

kind create cluster
This will just take a few minutes.

How easy is that?

You can now use kubectl from the command line. For example:

kubectl get pods -A
You will now see a list of all the pods and their statuses. They should all be showing ready.

Containers within containers
Kind implements each node in the cluster as a container within the development machine’s Docker environment. You can see this with:

docker container ls
Kind deploys all the required Kubernetes components within the docker image along with any container images that your Kubernetes cluster requires.

Container Network Interface (CNI)
If you have been following my articles, you will know that a Kubernetes cluster requires a CNI plugin. By default, Kind installs kindnetd and you will see this as a pod in your cluster.

You can disable this and run your own, such as Calico, if you require. For this article we will stay with the default.

Multiple Clusters
There are options to create multiple clusters and to be able to manage them independently but for now we will just use one. One option I will mention is to name your cluster:

kind create cluster --name <your cluster name>
Deleting the cluster
Should you want to delete your cluster, you can do with:

kind delete cluster --name <your cluster name>
If you have created a cluster with the default name, you can omit the cluster name.

Note that once deleted, your cluster and all its config and data is gone too!

Multiple nodes
So far the cluster we have created is a single node. Most production deployments are multi-node clusters.

We can create a multi-node cluster by providing a suitable configuration file.

For a single master with two worker nodes (similar to my cloud deployment), create the following file:

kind-config.yml

apiVersion: kind.x-k8s.io/v1alpha4
kind: Cluster
nodes:
- role: control-plane
  extraPortMappings:
  - containerPort: 30000
    hostPort: 30000
    listenAddress: "0.0.0.0" # Optional, defaults to "0.0.0.0"
    protocol: tcp # Optional, defaults to tcp
  - containerPort: 31321
    hostPort: 31321
  - containerPort: 31300
    hostPort: 31300
- role: worker
- role: worker
Note that we need to expose specific ports via Kind (extraPortMappings) so we can access them on our development machine. We will use ports 30000, 31321 and 31300 later in the article.

Now we can create our cluster again:

kind create cluster --name my-k8s --config kind-config.yml
This time you should see 3 nodes:

kubectl get nodes
We can now start installing our applications.

Creating and installing a docker image
One of the great things about using a tool such as Kind, is the ability to create and deploy docker images to your cluster without having to upload the image to a repository like Docker Hub.

To show how this works, we will create a simple docker image and then load it into our cluster. This will be a simple web server based on NGINX.

First create the folders:

mkdir sample-app
mkdir sample-app/files
Create the following files:

sample-app/files/index.html

<html>
  <head>
    <title>Dockerfile</title>
  </head>
  <body>
    <div class="container">
      <h1>My App</h1>
      <h2>This is my first app</h2>
      <p>Hello everyone, This is running via Docker container</p>
    </div>
  </body>
</html>
sample-app/files/default

server {
    listen 80 default_server;
    listen [::]:80 default_server;
    
    root /usr/share/nginx/html;
    index index.html index.htm;

    server_name _;
    location / {
        try_files $uri $uri/ =404;
    }
}
sample-app/Dockerfile

FROM ubuntu:24.04  
RUN  apt -y update && apt -y install nginx
COPY files/default /etc/nginx/sites-available/default
COPY files/index.html /usr/share/nginx/html/index.html
EXPOSE 80
CMD ["/usr/sbin/nginx", "-g", "daemon off;"]
Now we build the Docker image:

cd sample-app 
docker build -t sample-app:1.0 .
Once built, it can be loaded into the nodes within the cluster:

kind load docker-image sample-app:1.0 --name my-k8s
Deploying an image requires a Deployment resource to be applied to the cluster. Create this file:

my-deployment.yml

apiVersion: apps/v1
kind: Deployment
metadata:
  name: sample-app
  namespace: default
spec:
  replicas: 1
  selector:
    matchLabels:
      app: sample-app
  template:
    metadata:
      labels:
        app: sample-app
    spec:
      containers:
      - name: sample-app
        image: sample-app:1.0
        imagePullPolicy: Never
        ports:
        - containerPort: 80
---
apiVersion: v1
kind: Service
metadata:
  name: sample-app-svc
  namespace: default
spec:
  selector:
    app: sample-app
  type: NodePort
  ports:
  - name: http
    port: 80
    targetPort: 80
    nodePort: 30000
Note that the imagePullPolicy is set to Never as we installed this into our cluster earlier. This deployment also includes a NodePort service so we can access the service.

Now deploy the deployment with:

kubectl apply -f my-deployment.yml
Check your pod is running with:

kubectl get pods
As we have deployed to the default namespace, we do not have to specify it.

Now it has been deployed, you should be able to see the page being served at:

http://localhost:30000
Congratulations, you now have a local cluster up and running.

Useful tools and applications
Whilst Kind gives you a Kubernetes cluster, you generally need a number of tools and applications to make it useful.

To help assist with the installation, Helm is required and this can be installed on the development machine with:

brew install helm
I have selected a few applications to get you started.

Istio service mesh
Grafana/Loki monitoring
Postgres operator
Adding Istio
Istio is a service mesh that gives you greater control over your networking. If you wish to install Istio you can from a Helm chart.

First register the repository with your local Helm:

helm repo add istio https://istio-release.storage.googleapis.com/charts
helm repo update
We will add Istio to its own namespace, which we have to create:

kubectl create namespace istio-system
As Istio has its own Custom Resource Definitions (CRDs), we need to load these into Kubernetes:

helm install istio-base istio/base -n istio-system --set defaultRevision=default
Check it installed with:

helm ls -n istio-system
As we are using kindnetd as our CNI, we do not need to install istio CNI. The next step is to install the istiod control plane:

helm install istiod istio/istiod -n istio-system --wait
The --wait option tells Helm to wait until the pods are up and running. This can take a while.

You now have Istio installed. To ensure the sidecar is created for each pod, you need to annotate the namespace with istio-injection: enabled. As we do not create the default namespace where our test service appears, we need to add the label with:

kubectl label namespace default istio-injection=enabled --overwrite
kubectl get namespace -L istio-injection
As the test pod exists, you need to delete it to force Kubernetes to recreate it.

kubectl delete <pod name>
Now, when you get the pod details you will see it has 2/2 containers showing the Istio sidecar has been deployed alongside the main sample-app container.

You can now configure Istio as needed.

Adding Grafana and Loki
It is extremely important that you know what your application is doing once it is deployed. Typically you will need visibility of the metrics and logs it provides.

To gain that visibility, we will install Loki and Grafana into our cluster.

First we will add the required charts to our Helm repository:

helm repo add grafana https://grafana.github.io/helm-charts
helm repo update
Now we will create a monitoring namespace to hold our monitoring applications.

kubectl apply -f - <<EOF
apiVersion: v1
kind: Namespace
metadata:
  name: monitoring
  labels:
    istio-injection: enabled
EOF
As we are using Helm, we need to give the chart any values we want to override. We want the chart to include Loki (which it does by default), Grafana and Prometheus. Create the following file:

loki-config.yml

grafana:
  enabled: true
prometheus:
  enabled: true
loki:
  image:
    tag: 2.8.11
Note the need to upgrade the image tag for Loki as Grafana adds a default error filter to log volume queries that breaks the Loki query and gives an IDENTIFIER error.

And now install the chart with:

helm install loki grafana/loki-stack -n monitoring -f loki-config.yml
It may take a few minutes to start everything. You can check with:

kubectl get pods -n monitoring
You will see that it installs promtail and prometheus-node-exporter on each node so it can collect the logs and metrics from each node.

Once everything is up and running, we can look at accessing the Grafana console. To do this we need the username and password. These are created and stores as Kubernetes secrets and so you can find them with:

kubectl get secret loki-grafana -n monitoring -o jsonpath={.data.admin-user} | base64 -d; echo
kubectl get secret loki-grafana -n monitoring -o jsonpath={.data.admin-password} | base64 -d; echo
If you look at the services that are created:

kubectl get svc -n monitoring -o wide
You will see that they are all ClusterIP services, which are only accessible internally. To access the Grafana console we will need a NodePort service to make the service available externally via the cluster nodes. Create the following file:

grafana-svc.yml

apiVersion: v1
kind: Service
metadata:
  name: grafana
  namespace: monitoring
spec:
  selector:
    app.kubernetes.io/instance: loki
    app.kubernetes.io/name: grafana
  type: NodePort
  ports:
  - port: 3000
    targetPort: 3000
    nodePort: 31300
We target port 3000 but expose it externally as 31300 due to the restrictions on allocating ports on the nodes (30000 to 32767).

Now create this service with:

kubectl apply -f grafana-svc.yml
You should now be able to go to your browser and access the Grafana console at: http://localhost:31300.

You can log in with the username and password you extracted earlier. As promtail has been deployed on each node, Loki captures all logs and can be queried by Grafana for display. This includes the logs for the Kubernetes components also.

You should be able to explore the logs of the sample app with a Grafana Loki filter of {app="sample-app"} |= ``. You can also look at the metrics too with a prometheus query such as node_memory_Active_bytes.

Adding Postgres
Before we add a database, we need to understand how Kind handles storage. In a Kubernetes cluster, there can be many different types of storage, each referred to by its storageClass name.

You can see the storage class provided by Kind using:

kubectl get storageclasses
Storage classes are defined at the cluster level and so no namespace is required.

You will see a result like this:

NAME                 PROVISIONER             RECLAIMPOLICY   VOLUMEBINDINGMODE      ALLOWVOLUMEEXPANSION   AGE
standard (default)   rancher.io/local-path   Delete          WaitForFirstConsumer   false                  12m
This shows that the storageClass is named standard and is the one that will be used if no class is provided: (default). This storage class ensure that the underlying persistent storage is associated with a pod on a given node. If the pod fails, it will attempt to be rescheduled on the same node so it can be connected to the same persistent storage or volume (PV). If it cannot, the pod will fail to schedule.

This is all done with the Rancher local-path provisioner.

To deploy our database we will use a Postgres provisioner known as an operator. The provisioner we will use is called CloudNativePG. We need to first update our Helm repository:

helm repo add cnpg https://cloudnative-pg.github.io/charts
helm repo update
Now we make a namespace for our database and annotate it for Istio:

kubectl apply -f - <<EOF
apiVersion: v1
kind: Namespace
metadata:
  name: pg
  labels:
    istio-injection: enabled
EOF
This also shows you how to use the heredoc format on the command line to create the namespace.

Now install the actual operator:

helm install cnpg cnpg/cloudnative-pg -n pg
With the operator configured, we can now create a Postgres database. We will create a default postgres (required by the operator) database, a myapp database, a postgres user (required by the operator) and an app-user. For the users we will create secrets to hold their credentials.

Secrets are stored with base 64 encoding to allow any characters to be stored.

In this example we will create:

postgres / super-secret
app-user / app-secret
Or, in base 64:

cG9zdGdyZXM= / c3VwZXItc2VjcmV0
YXBwLXVzZXI= / YXBwLXNlY3JldA==
Create the following file:

db-user-config.yml

apiVersion: v1
kind: Secret  
type: kubernetes.io/basic-auth
metadata:
  name: pg-superuser
  namespace: pg
data:
  password: c3VwZXItc2VjcmV0
  username: cG9zdGdyZXM=

---

apiVersion: v1
kind: Secret  
type: kubernetes.io/basic-auth
metadata:
  name: app-user
  namespace: pg
data:
  password: YXBwLXNlY3JldA==
  username: YXBwLXVzZXI=
And create these secrets with:

kubectl apply -f db-user-config.yml
Now let’s create our database cluster (the operator calls it a cluster even though we will only create 1 replica). We will also create a NodePort service to expose the database on port 31321 so we can attach to it using a database client, such as DBeaver. We exposed port 31321 earlier when we created the cluster.

The is the database definition we will use. It is a cut down version of one I provided in another article.

db-config.yml

apiVersion: postgresql.cnpg.io/v1
kind: Cluster
metadata:
  name: db-cluster
  namespace: pg
  labels:
    cnpg.io/reload: ""
spec:
  description: "Postgres database cluster"
  imageName: ghcr.io/cloudnative-pg/postgresql:15.1
  instances: 1

  superuserSecret:
    name: pg-superuser

  managed:
    roles:
      - name: app-user
        ensure: present
        comment: user for application
        login: true
        superuser: false
        passwordSecret:
          name: app-user

  enableSuperuserAccess: true

  postgresql:
    pg_hba:
    - host all all 10.0.0.0/8 scram-sha-256
    - hostssl all all 10.0.0.0/8 scram-sha-256
    - hostssl all all 192.0.0.0/8 scram-sha-256
    - host all all 172.0.0.0/8 scram-sha-256
    - host all all 127.0.0.0/24 scram-sha-256
    - host all all all scram-sha-256

  bootstrap:
    initdb:
      database: myapp
      owner: app-user
      secret:
        name: app-user
      postInitApplicationSQL:
        - create schema myapp authorization "app-user"
        - grant all on schema myapp to "app-user"
        - grant all on all tables in schema myapp to "app-user" 

  storage:
    size: 10Gi
    storageClass: standard

---

apiVersion: v1
kind: Service
metadata:
  name: pg
  namespace: pg
  labels:
    name: pg
spec:
  type: NodePort
  ports:
  - name: http
    port: 5432
    targetPort: 5432
    nodePort: 31321
  selector:
    cnpg.io/cluster: db-cluster
The pg_hba section, or stanza, deliberately lists many options to demonstrate how to set up security around your database. At the end of the stanza, the line host all all all scram-sha-256 gives everyone access to everything from everywhere. Whilst this is fine for experimentation, do not do this in production.

You will notice that we create the NodePort service in the second document (--section) in the file.

We now create the cluster and the service with:

kubectl apply -f db-config.yml
This can take a few minutes so check everything is running with:

kubectl get pods -n pg
You should now be able to connect to your database on local:31321 using a database client and the usernames and passwords above. Connect to the postgres database.

If using DBeaver, remember to tick the box to show all databases or else you will not see the myapp database.

You should now see two databases, postgres and myapp. The second one is the one you should use for your applications with the user app-user, who we defined earlier. Note that there is also a myapp schema within this database.

Summary
In this article, we have explored how to set up a Kubernetes cluster on our local development machine using Kind.

We created a Docker image and deployed it without the need to push it to a repository first.

Once we did that, we installed networking, monitoring and a database to make the platform ready for application development.

This gives us a multi-node Kubernetes cluster on which we can base our application development.

Although all the code you require is in the article above, you can also find it in my GitHub repository.

If you found this article of interest, please give me a clap as that helps me identify what people find useful and what future articles I should write. If you have any suggestions, please add them in the comments section.