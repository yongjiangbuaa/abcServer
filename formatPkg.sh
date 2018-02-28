#TMP_FILE=tmp
#echo $1 > $TMP_FILE
#sed -i.bak 's=gow_backend/ClashOfKingProject/cok-game/src/main/java/com/elex/cok=netty-server/src/main/java/com/geng=g'  $TMP_FILE 
#DEST_PAH=`cat $TMP_FILE`
DEST_PATH=`echo $1 | sed 's=gow_backend/ClashOfKingProject/cok-game/src/main/java/com/elex/cok=netty-server/src/main/java/com/geng=g; s=gow_backend/ClashOfKingProject/cok-game/src/main/resources/com/elex/cok=netty-server/src/main/resources/com/geng=g'`
echo dest_path:$DEST_PATH
cp $1 $DEST_PATH
echo "$1 copied to dest_path"

# get base name
#TMP1=`echo $1 | sed 's=/=;=g'`
#FILE_NAME=`echo $TMP1 | awk -F";" '{ print $NF}' `
DEST_FILE=$DEST_PATH
echo dest_file=$DEST_FILE
sed -i.bak 's/elex\.cok/geng/g' $DEST_FILE
sed -i.bak 's/smartfoxserver\.v2\.entities/geng\.core/g' $DEST_FILE
rm -rf $DEST_FILE.bak

