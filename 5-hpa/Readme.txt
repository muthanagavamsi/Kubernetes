Run 

kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.5.0/components.yaml

kubectl patch deployment metrics-server -n kube-system --patch "$(cat 2-metric-server-patch.yaml)"