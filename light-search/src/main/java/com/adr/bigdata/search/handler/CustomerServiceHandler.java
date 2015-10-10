package com.adr.bigdata.search.handler;

import org.apache.solr.common.util.NamedList;

import com.adr.bigdata.search.handler.query.frontend.CustomerServiceQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.FrontendResponseExecutor;

@SuppressWarnings("rawtypes")
public class CustomerServiceHandler extends AdrSearchBaseHandler {
	public void init(NamedList args) {
		super.init(args);
		builder = new CustomerServiceQueryBuilder();
		responseExecutor = new FrontendResponseExecutor();
	}
}
