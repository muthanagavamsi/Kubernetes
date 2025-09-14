Install metrics server.

kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/latest/download/components.yaml

kubectl -n kube-system patch deployment/metrics-server \
  --type=json \
  --patch='[{"op": "add", "path": "/spec/template/spec/containers/0/args/-", "value": "--kubelet-insecure-tls"}]'

kubectl get deployment metrics-server -n kube-system

k top pods

-------

------
Run http service from /d/Vamsi/3-Tech-Projects/Kubernetes/29-inplace

k apply -f resize.yaml
-------

------

Run Jumpbox
k run jumpbox --image=nginx -i --tty -- bash

Install ab
apt install apache2-utils

Load test.
time ab -n 1000 -c 20 http://10.244.1.11/

-------

Increase cpu

kubectl patch pod resize-test --subresource resize --patch \
'{"spec":{"containers":[{"name":"app","resources":{"limits":{"cpu":"200m"},"requests":{"cpu":"200m"}}}]}}'

kubectl get pod resize-test -o jsonpath='{.spec.containers[0].resources}'
kubectl get pod resize-test -o jsonpath='{.status.containerStatuses[0].resources}'

Increase memory

kubectl patch pod resize-test --subresource resize --patch \
'{"spec":{"containers":[{"name":"app","resources":{"limits":{"memory":"400Mi"},"requests":{"memory":"400Mi"}}}]}}'

------

Load test.
time ab -n 1000 -c 20 http://10.244.1.11/
------
