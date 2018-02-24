#TMP_FILE=tmp
#echo $1 > $TMP_FILE
#sed -i.bak 's=gow_backend/ClashOfKingProject/cok-game/src/main/java/com/elex/cok=netty-server/src/main/java/com/geng=g'  $TMP_FILE 
#DEST_PAH=`cat $TMP_FILE`
#echo "$DEST_PATH"
#cp $1 $DEST_PATH
#echo `head $DEST_PATH`
sed -i.bak 's/elex\.cok/geng/g' $1
sed -i.bak 's/smartfoxserver\.v2\.entities/geng\.core/g' $1
rm -rf $1.bak

