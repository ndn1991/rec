package com.adr.bigdata.search.handler;

import org.apache.solr.common.util.NamedList;

import com.adr.bigdata.search.handler.query.deal.DealQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.DealResponseExecutor;

@SuppressWarnings("rawtypes")
public class DealRequestHandler extends AdrSearchBaseHandler {
	@Override
	public void init(NamedList args) {
		super.init(args);
		super.builder = new DealQueryBuilder();
		super.responseExecutor = new DealResponseExecutor();
	}
}
