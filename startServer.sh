java -cp ./lib/slf4j-api-1.7.25.jar:./lib/netty-all-4.1.19.Final.jar:build/classes/main -Xrunjdwp:transport=dt_socket,address=9796,server=y,suspend=n com.geng.server.Server > server.log &2>&1 &
