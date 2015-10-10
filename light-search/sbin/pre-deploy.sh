if [ -z "$hazelcast_group_name" ]; then 
	echo "hazelcast_group_name bat buoc phai ton tai"
	exit 1
fi
if [ -z "$hazelcast_group_pass" ]; then 
	echo "hazelcast_group_pass bat buoc phai ton tai"
	exit 1
fi
if [ -z "$hazelcast_join_host" ]; then 
	echo "hazelcast_join_host bat buoc phai ton tai"
	exit 1
fi
if [ -z "$db_host" ]; then 
	echo "db_host bat buoc phai ton tai"
	exit 1
fi
if [ -z "$db_port" ]; then 
	echo "db_port bat buoc phai ton tai"
	exit 1
fi
if [ -z "$db_user" ]; then 
	echo "db_user bat buoc phai ton tai"
	exit 1
fi
if [ -z "$db_pass" ]; then 
	echo "db_pass bat buoc phai ton tai"
	exit 1
fi
if [ -z "$tracking_user" ]; then 
	echo "tracking_user bat buoc phai ton tai"
	exit 1
fi
if [ -z "$tracking_pass" ]; then 
	echo "tracking_pass bat buoc phai ton tai"
	exit 1
fi
if [ -z "$deal_user" ]; then 
	echo "deal_user bat buoc phai ton tai"
	exit 1
fi
if [ -z "$deal_pass" ]; then 
	echo "deal_pass bat buoc phai ton tai"
	exit 1
fi


#hazelcast-client.xml 
sed -i -- "s/@name_hazelcast/$hazelcast_group_name/g" big-data/conf/hazelcast-client.xml
sed -i -- "s/@pass_hazelcast/$hazelcast_group_pass/g" big-data/conf/hazelcast-client.xml
# do something rat kinh khung
arr=$(echo $hazelcast_join_host | tr "," "\n")
hosts=""
for x in $arr
do
    hosts="$hosts<address>$x<\/address>"
done
echo "hosts: $hosts"
sed -i -- "s/@iphazelcast/$hosts/g" big-data/conf/hazelcast-client.xml

#system.properties
sed -i -- "s/@ip/$db_host/g" big-data/conf/system.properties
sed -i -- "s/@port/$db_port/g" big-data/conf/system.properties
sed -i -- "s/@username_cm/$db_user/g" big-data/conf/system.properties
sed -i -- "s/@pass_cm/$db_pass/g" big-data/conf/system.properties
sed -i -- "s/@db_name/$db_name/g" big-data/conf/system.properties
#sed -i -- "s/@hostlist/$solrcloud_hosts/g" big-data/conf/system.properties

sed -i -- "s/@ip/$db_host/g" product/conf/db-data-config.xml
sed -i -- "s/@port/$db_port/g" product/conf/db-data-config.xml
sed -i -- "s/@cm_db_name/$db_name/g" product/conf/db-data-config.xml
sed -i -- "s/@username_cm/$db_user/g" product/conf/db-data-config.xml
sed -i -- "s/@pass_cm/$db_pass/g" product/conf/db-data-config.xml
sed -i -- "s/@username_tracking/$tracking_user/g" product/conf/db-data-config.xml
sed -i -- "s/@pass_tracking/$tracking_pass/g" product/conf/db-data-config.xml

sed -i -- "s/@ip/$db_host/g" deal/conf/db-data-config.xml
sed -i -- "s/@port/$db_port/g" deal/conf/db-data-config.xml
sed -i -- "s/@username_deal/$deal_user/g" deal/conf/db-data-config.xml
sed -i -- "s/@pass_deal/$deal_pass/g" deal/conf/db-data-config.xml

sed -i -- "s/@ip/$db_host/g" merchant/conf/db-data-config.xml
sed -i -- "s/@port/$db_port/g" merchant/conf/db-data-config.xml
sed -i -- "s/@cm_db_name/$db_name/g" merchant/conf/db-data-config.xml
sed -i -- "s/@username_cm/$db_user/g" merchant/conf/db-data-config.xml
sed -i -- "s/@pass_cm/$db_pass/g" merchant/conf/db-data-config.xml

#sed -i -- "s/@jenkinhost/$jenkinhost/g" deploy.sh
#sed -i -- "s/@jenkinuser/$jenkinuser/g" deploy.sh

echo "echo thanh cong phat cho hung khoi"