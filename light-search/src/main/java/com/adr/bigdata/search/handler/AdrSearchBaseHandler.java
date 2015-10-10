package com.adr.bigdata.search.handler;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CloseHook;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.ResponseStrategy;
import com.nhb.common.Loggable;

@SuppressWarnings({ "rawtypes" })
public class AdrSearchBaseHandler extends SearchHandler implements Loggable {
	protected ResponseStrategy responseExecutor;
	protected QueryBuilder builder;

	@Override
	public void init(NamedList args) {
		// init SQL connection pools or other resource here
		super.init(args);
	}

	@Override
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
		if (builder == null || responseExecutor == null) {
			getLogger().error("solr handler error: not init builder and responseExecutor...");
			return;
		}
		req.setParams(builder.buildSolrQuery(req));
		super.handleRequest(req, rsp);
		responseExecutor.execute(rsp, req);
	}

	@Override
	public void inform(SolrCore core) {
		core.addCloseHook(new CloseHook() {
			@Override
			public void preClose(SolrCore sc) {
				getLogger().info("closing solr core...{}", this.getClass());
			}

			@Override
			public void postClose(SolrCore sc) {

			}
		});
		super.inform(core);
	}

}
