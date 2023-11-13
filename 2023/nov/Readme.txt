kubectl apply -f pod-init.yaml

kubectl exec -it webapp-pod -c webapp -- cat /usr/share/nginx/html/config/config.txt

#Test total initi and normal containers on pod. 

kubectl get pod webapp-pod -o jsonpath='{.spec.containers[*].name},{.spec.initContainers[*].name}' | tr ',' '\n'


