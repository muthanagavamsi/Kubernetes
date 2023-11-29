echo "kubectl apply -f test.yaml -n test" > 1-run.sh
echo "kubectl get pods -n test -o wide -w" > 2-show.sh
echo "kubectl get events -n test -w" > 3-events.sh
echo "kubectl delete pod example-pod -n test" > 4-delete.sh
echo "kubectl exec -it example-pod -c rc1 -n test -- /bin/sh -c "hostname"" > test.sh
chmod +x *