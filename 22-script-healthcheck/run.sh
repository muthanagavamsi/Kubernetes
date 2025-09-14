for url in livez healthz readyz
do 
 echo "================ $url ================"
 kubectl get --raw="/$url?verbose"
done
