for (( ; ; ))
do
  curl --silent --output /dev/null --write-out "%{http_code}" http://10.96.196.228
  echo   # to print a newline after each status code
  sleep 1  # optional: wait 1 second between requests
done
