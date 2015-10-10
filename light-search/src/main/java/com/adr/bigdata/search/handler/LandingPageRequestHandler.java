/**
 * 
 */
package com.adr.bigdata.search.handler;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.query.frontend.LandingPageQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.FrontendResponseExecutor;

/**
 * @author minhvv2
 *
 */
public class LandingPageRequestHandler extends AdrSearchBaseHandler {
	@Override
	public void init(NamedList args) {
		super.init(args);
		responseExecutor = new FrontendResponseExecutor();
		builder = new LandingPageQueryBuilder();
	}

	@Override
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
		super.handleRequest(req, rsp);
	}

}
