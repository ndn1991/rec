/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.responsestrategy;

import java.io.IOException;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.utils.SolrDocumentUtils;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;

/**
 *
 * @author Tong Hoang Anh
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class FrontendResponseExecutor implements ResponseStrategy {

	@Override
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		ResultContext response = (ResultContext) rsp.getValues().get("response");
		rsp.getValues().remove("response");
		if (response != null) {
			/*
			 * response: { numFound: 0, start: 0, docs: [ ] }
			 */
			try {
				SolrDocumentList docList = SolrDocumentUtils.getSolrDocumentList(response.docs, rsp, req);
				rsp.add("listProduct", docList);// syncronizePrice(spMapResult,
				// rsp, req));
				rsp.add("numFound", response.docs.matches());
			} catch (IOException e) {
				rsp.add("listProduct", new NamedList());
				rsp.add("numFound", 0);
			}

		} else {
			rsp.add("listProduct", new NamedList());
			rsp.add("numFound", 0);
		}

		writeSpellCheck(rsp);

	}

	private void writeSpellCheck(SolrQueryResponse rsp) {
		NamedList spellCheck = (NamedList) SolrQueryResponseHelper.erase(rsp, "spellcheck");

		if (spellCheck != null) {
			NamedList collation = (NamedList) spellCheck.get("collations");
			if (collation != null) {
				Object bestCollection = collation.get("collation");
				SolrQueryResponseHelper.write2Response(rsp, "spellcheck", bestCollection);
			}
		} else {
			SolrQueryResponseHelper.write2Response(rsp, "spellcheck", "");
		}
	}

}
