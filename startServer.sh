CPATH="config/:lib/*:build/libs/netty-server.jar"
java -cp ${CPATH} -Xrunjdwp:transport=dt_socket,address=9796,server=y,suspend=n com.geng.server.Server >> server.log &2>&1 &

#java -cp .:./config/logback.xml:./lib/logback-classic-1.2.3.jar:./lib/logback-core-1.2.3.jar:./lib/slf4j-api-1.7.25.jar:./lib/netty-all-4.1.19.Final.jar:build/classes/main -Xrunjdwp:transport=dt_socket,address=9796,server=y,suspend=n com.geng.server.Server >> server.log &2>&1 &
