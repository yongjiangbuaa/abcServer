COK_JAVA=$GOW/ClashOfKingProject/cok-game/src/main/java/com/elex/cok/
ABC_JAVA=$NETTY_PROJ/src/main/java/com/geng/
cp $COK_JAVA/puredb/model/UserLord.java $ABC_JAVA/puredb/model/
#cp /var/root/Documents/gow_backend/ClashOfKingProject/cok-game/src/main/java/
sed -i.bak 's/elex\.cok/geng/g' $1
sed -i.bak 's/smartfoxserver\.v2\.entities/geng\.core/g' $1
rm -rf $1.bak
