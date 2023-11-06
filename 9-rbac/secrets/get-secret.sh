kubectl exec -it deployment2-f78cc8d5b-7f98l -- sh -c "echo $(kubectl get secret my-secret -o jsonpath='{.data.my-key}')"
