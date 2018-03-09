echo 0=$0
echo 1=$1
echo 2=$2
echo 3=$3
ARGS=()
for a in "$@"; do
    ARGS+=("${a}")
done
echo args=$ARGS
ARG=`echo $1|sed 's/,/;/g'`
WWW=123.206.90.153:9933
LOCAL=10.1.36.91:8080
IP=$LOCAL
if [ -n $2 ]
then
	IP=$LOCAL
fi
curl -d $ARG -X POST http://$IP 
#CMD=curl -d $ARG{"deviceId":"1"} -X POST http://$IP 
#echo "CMD=$CMD"
#`$CMD`
