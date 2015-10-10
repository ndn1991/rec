package com.adr.bigdata.search.handler.responsestrategy;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.utils.SolrDocumentUtils;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;

public class RelatedProductResponseExecutor implements ResponseStrategy {
	@Override
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		ResultContext response = (ResultContext) SolrQueryResponseHelper.erase(rsp, "response");
		if (response != null) {
			try {
				SolrDocumentList docList = SolrDocumentUtils.getSolrDocumentList(response.docs, rsp, req);
				rsp.add("listProduct", docList);
			} catch (Exception ex) {

			}

		} else {
			rsp.add("listProduct", new NamedList());
		}
	}

}
