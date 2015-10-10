/**
 * 
 */
package com.adr.bigdata.search.handler;

import java.util.List;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.firstclassfunction.Function;
import com.adr.bigdata.search.handler.query.frontend.ComboSearchQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.ComboSearchResponseStrategy;
import com.adr.bigdata.search.handler.utils.SolrQueryRequestHelper;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 * 
 * @input: list productIds output:
 * @return: list available (con hang) productItemIds along with district, city
 *          information
 */
public class ComboSearchRequestHandler extends SearchHandler implements Loggable {

	private Function transformer = new Function<String, String>() {
		@Override
		public String apply(String t) {
			return t;
		}
	};
	private ComboSearchResponseStrategy responseExecutor;
	private ComboSearchQueryBuilder builder;

	@Override
	public void init(NamedList args) {
		super.init(args);
		responseExecutor = new ComboSearchResponseStrategy();
		builder = new ComboSearchQueryBuilder();
	}

	@Override
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
		List<String> productIds = SolrQueryRequestHelper.extractListParams(req, "productIds", ",", transformer);
		req.setParams(builder.buildSolrQuery(req));
		super.handleRequest(req, rsp);
		responseExecutor.handleResponse(rsp, req, productIds);
	}

}
