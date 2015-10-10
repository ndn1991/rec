curl -sS 'http://localhost:8983/solr/merchant/dataimport?command=full-import&clean=false&entity=deal'
curl -sS 'http://localhost:8983/solr/deal/dataimport?command=full-import&clean=false&entity=deal'
curl -sS 'http://localhost:8983/solr/product/dataimport?command=full-import&clean=false&entity=product_element'