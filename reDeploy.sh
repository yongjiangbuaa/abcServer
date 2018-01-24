JPS_RES=`jps | grep Server | awk '{print $1}'`
echo "Server pid: $JPS_RES"
kill $JPS_RES
echo "...killed"
./build.sh
./startServer.sh

