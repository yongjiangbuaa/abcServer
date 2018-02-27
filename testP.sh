echo 0=$0
echo 1=$1
WWW=123.206.90.153:9933
LOCAL=10.1.33.220:8080
IP=$LOCAL
#if [ -n $2 ]
#then
#	IP=$WWW
#fi
CMD="curl -d '$1' -X POST http://$IP "
echo "CMD=$CMD"
`$CMD`
