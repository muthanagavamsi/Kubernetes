alias k='kubectl'
alias kgp='kubectl get pods -o wide'
alias kgd='kubectl get deployments'
alias kgs='kubectl get services'
alias kgn='kubectl get nodes'
alias kaf='kubectl apply -f'
alias kgn='kubectl get nodes'

------

1: kubectl create ns test
2: create test.yaml and paste the ivan's sidecar yaml
3: 
------

Terminate a container in a pod. Testing init container termination along with sidecar.
kubectl exec -it example-pod -c sc1 -n test -- /bin/sh -c "exit 1"
kubectl exec -it example-pod -c rc1 -n test -- /bin/sh -c "echo vamsi"

------

for i in {1..100}; do echo $i; done


