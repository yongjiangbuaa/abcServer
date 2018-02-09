sed -i.bak 's/elex\.cok/geng/g' $1
sed -i.bak 's/smartfoxserver\.v2\.entities/geng\.core/g' $1
rm -rf $1.bak

