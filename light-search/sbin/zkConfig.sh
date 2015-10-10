/opt/solr/server/scripts/cloud-scripts/zkcli.sh -zkhost  localhost:9093 -cmd clear /configs
/opt/solr/server/scripts/cloud-scripts/zkcli.sh -cmd upconfig -zkhost  localhost:9093 --confname product --solrhome  $SOLR_HOME --confdir $SOLR_PRODUCT_CONFIG
/opt/solr/server/scripts/cloud-scripts/zkcli.sh -cmd upconfig -zkhost  localhost:9093  --confname deal --solrhome  $SOLR_HOME --confdir $SOLR_DEAL_CONFIG
/opt/solr/server/scripts/cloud-scripts/zkcli.sh -cmd upconfig -zkhost  localhost:9093  --confname merchant --solrhome  $SOLR_HOME --confdir $SOLR_MERCHANT_CONFIG
/opt/solr/server/scripts/cloud-scripts/zkcli.sh -cmd upconfig -zkhost  localhost:9093  --confname rec --solrhome  $SOLR_HOME --confdir /opt/light-search/rec/conf
