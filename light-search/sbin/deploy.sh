echo -e "#============================================================================================================================================#"eecho -e	"# 	       	       	       	       	       	       	       	       	       	       	       	       	       	       	       	       	     #"eecho -e "#                Hay chac chan la cac thu muc sau duoc di kem voi file .sh nay 'deal', 'product', 'merchant', 'big-data'                     #"
echo -e	"#    	       	       	       	       	       	       	       	       	       	       	       	       	       	       	       	       	     #"
echo -e	"#============================================================================================================================================#\n\n"

echo "Path to solr $1"
if [ ! -d "$1" ]; then
	echo -e "\nsolr directory is not exist"
	exit 1
fi

SOLR_DIR=$1
DIST_DIR=$SOLR_DIR/dist
SOLR_HOME=$SOLR_DIR/server/solr

echo "SOLR_DIR - $SOLR_DIR"
echo "DIST_DIR - $DIST_DIR"
echo "SOLR_HOME - $SOLR_HOME"

if [ ! -d "$DIST_DIR" ]; then
	echo -e "\nSOLR_DIR/dist directory is not exist"
	exit 2
fi
if [ ! -d "$SOLR_HOME" ]; then
	echo -e "\nSOLR_DIR/server/solr is not exits"
	exit 3
fi
if [ ! -d "product" ]; then
	echo -e "\nproduct core config is not exits, check source again from git"
	exit 4
fi
if [ ! -d "deal" ]; then
	echo -e "\ndeal core config is not exits, check source again from git"
	exit 5
fi
if [ ! -d "rec" ]; then
	echo -e "\rec core config is not exits, check source again from git"
	exit 6
fi
if [ ! -d "big-data" ]; then
	echo -e "\nbig-data core config is not exits, check source again from git"
	exit 7
fi

WORKING_DIR=${PWD}
ln -sfn $WORKING_DIR /opt/light-search
cd $DIST_DIR
ln -sfn $WORKING_DIR/big-data big-data
cd $SOLR_HOME
ln -sfn $WORKING_DIR/product product
ln -sfn $WORKING_DIR/deal deal
ln -sfn $WORKING_DIR/rec rec