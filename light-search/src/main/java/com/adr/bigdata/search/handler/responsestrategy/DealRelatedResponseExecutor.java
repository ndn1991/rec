package com.adr.bigdata.search.handler.responsestrategy;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;

import com.nhb.common.Loggable;

public class DealRelatedResponseExecutor implements ResponseStrategy, Loggable {

	@Override
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		ResultContext response = (ResultContext) rsp.getValues().get("response");
		rsp.getValues().remove("response");
		if (response != null) {
			try {
				rsp.add("listDeal", response.docs);
				rsp.add("numFound", response.docs.matches());
			} catch (Exception e) {
				rsp.add("listDeal", new NamedList<Object>());
				rsp.add("numFound", 0);
			}
		} else {
			rsp.add("listDeal", new NamedList<Object>());
			rsp.add("numFound", 0);
		}
	}

}
