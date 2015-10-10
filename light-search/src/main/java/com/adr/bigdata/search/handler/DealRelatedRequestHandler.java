package com.adr.bigdata.search.handler;

import org.apache.solr.common.util.NamedList;

import com.adr.bigdata.search.handler.query.deal.DealRelatedQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.DealRelatedResponseExecutor;

public class DealRelatedRequestHandler extends AdrSearchBaseHandler {
	@SuppressWarnings("rawtypes")
	@Override
	public void init(NamedList args) {
		super.init(args);
		super.builder = new DealRelatedQueryBuilder();
		super.responseExecutor = new DealRelatedResponseExecutor();
	}
}
