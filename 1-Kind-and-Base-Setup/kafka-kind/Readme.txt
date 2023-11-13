alias k='kubectl'
alias kgp='kubectl get pods -o wide'
alias kgd='kubectl get deployments'
alias kgs='kubectl get services'
alias kgn='kubectl get nodes'

---------

Hi. Let's learn Kubernetes.

#Create Kind cluster.
bash kind-create.sh

#list all clusters
kind get clusters
kubectl cluster-info --context kind-my-cluster


#Delete Kind cluster.
bash kind-delete.sh

#--> Inside the kind cluster

#Login into control-plane using docker.
docker exec -it my-cluster-control-plane bash

#Install ping, telnet, ifconfig, bash, vim
#apt-cache search iputils-ping
apt update
apt-get install iputils-ping
apt-get install telent
apt-get install net-tools
apt-get install bash
apt-get install vim

#-----------------

#--> If required - Set up local apine container. Specific to project.

#---------------
docker run -it -v ${PWD}:/work -w /work alpine sh
#----------------
