curl -s 'http://localhost:8983/solr/merchant/dataimport?command=full-import&clean=false&entity=deal';
curl -s 'http://localhost:8983/solr/product/dataimport?command=full-import&clean=false&entity=warehouse_product_item&update.chain=vin_update_chain';