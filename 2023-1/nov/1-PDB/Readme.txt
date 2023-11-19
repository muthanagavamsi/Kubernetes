kubectl apply -f nginx-deployment.yaml

kubectl create poddisruptionbudget nginx-pdb --namespace=default --min-available=4 --selector=app=nginx --dry-run=client -o yaml --save-config | kubectl apply -f -

kubectl scale deployment nginx-deployment --replicas=3

