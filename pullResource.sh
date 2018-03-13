OLD_DIR=`pwd`
XML_DIR=$DEPLOY_DIR/resource/xml/server
cd $XML_DIR
echo go to $XML_DIR
svn up
cd $OLD_DIR
echo back to $OLD_DIR
rsync -cvzr  $XML_DIR/* resource/
