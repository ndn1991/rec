package com.adr.bigdata.search.handler;

import org.apache.solr.common.util.NamedList;

import com.adr.bigdata.search.handler.query.frontend.RelatedProductQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.RelatedProductResponseExecutor;

public class RelatedProductHandler extends AdrSearchBaseHandler {

	public void init(@SuppressWarnings("rawtypes") NamedList args) {
		super.init(args);
		builder = new RelatedProductQueryBuilder();
		responseExecutor = new RelatedProductResponseExecutor();
	}
}
