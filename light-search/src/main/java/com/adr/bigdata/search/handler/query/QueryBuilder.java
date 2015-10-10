package com.adr.bigdata.search.handler.query;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

public abstract class QueryBuilder {
	public abstract SolrParams buildSolrQuery(SolrQueryRequest request);	
}
