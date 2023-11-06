#Link

https://gist.github.com/sanketsudake/a089e691286bf2189bfedf295222bd43

STEPS:

1:

Deploy latest metric-server release.

kubectl apply -f https://github.com/kubernetes-sigs/metrics-server/releases/download/v0.5.0/components.yaml

2:

Within existing arguments to metric-server container, you need to add argument  --kubelet-insecure-tls.

The file -> metric-server-patch.yaml already created, now run.

kubectl patch deployment metrics-server -n kube-system --patch "$(cat metric-server-patch.yaml)"
