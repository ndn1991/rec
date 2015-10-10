package com.adr.bigdata.search.handler;

import org.apache.solr.common.util.NamedList;

import com.adr.bigdata.search.handler.query.deal.DealHomeQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.DealHomeResponseExecutor;

public class DealHomeRequestHandler extends AdrSearchBaseHandler {
	@SuppressWarnings("rawtypes")
	@Override
	public void init(NamedList args) {
		super.init(args);
		super.builder = new DealHomeQueryBuilder();
		super.responseExecutor = new DealHomeResponseExecutor();
	}
}
