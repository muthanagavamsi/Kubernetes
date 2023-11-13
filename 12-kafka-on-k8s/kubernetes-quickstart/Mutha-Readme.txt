1: Install the monitoring on our Kubernetes cluster with the kube-prometheus-stack. 
- Let’s add the following Helm repository: 

helm repo add prometheus-community \
    https://prometheus-community.github.io/helm-charts

2: Install the chart in the monitoring namespace.  

k get ns
helm install kube-prometheus-stack \
    prometheus-community/kube-prometheus-stack \
    --version 52.1.0 -n monitoring --create-namespace

3:Install Strimzi Operator on Kubernetes

helm repo add strimzi https://strimzi.io/charts

4: Go to directory "12-kafka-on-k8s/kubernetes-quickstart/kafka" create this - strimzi-values.yaml

Add following config.

dashboards:
  enabled: true
  namespace: monitoring
featureGates: +UseKRaft,+KafkaNodePools,+UnidirectionalTopicOperator

5: Run the helm charts. 

helm install strimzi-kafka-operator strimzi/strimzi-kafka-operator \
    --version 0.38.0 \
    -n strimzi --create-namespace \
    -f strimzi-values.yaml