shutdown.sh
./build.sh
cp -R ~/Documents/netty-server/build/classes/main/*  $CATALINA_HOME/webapps/sample/WEB-INF/classes/
#cp ~/Documents/netty-server/lib/* /var/root/Downloads/apache-tomcat-9.0.2/webapps/sample/WEB-INF/lib/
echo "cp -R ~/Documents/netty-server/build/classes/main/*  $CATALINA_HOME/webapps/sample/WEB-INF/classes/"
startup.sh
