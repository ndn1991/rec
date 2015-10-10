package com.adr.bigdata.search.handler.query.deal;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.nhb.common.Loggable;

public class DealHomeQueryBuilder extends QueryBuilder implements Loggable {

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		DealHomeQuery query = new DealHomeQuery(request);
		return query.getQueryMap();
	}
}
