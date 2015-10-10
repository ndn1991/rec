package com.adr.bigdata.search.handler.utils;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResponseWriterUtil;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.TransformContext;
import org.apache.solr.search.DocList;

public class SolrDocumentUtils {
	public static SolrDocumentList getSolrDocumentList(DocList docs, SolrQueryResponse rsp, SolrQueryRequest req)
			throws IOException {

		SolrDocumentList returnList = new SolrDocumentList();
		TransformContext context = new TransformContext();
		context.req = req;

		DocTransformer transformer = rsp.getReturnFields().getTransformer();
		context.searcher = req.getSearcher();
		context.iterator = docs.iterator();
		if (transformer != null) {
			transformer.setContext(context);
		}
		int sz = docs.size();
		Set<String> fnames = rsp.getReturnFields().getLuceneFieldNames();
		for (int i = 0; i < sz; i++) {
			int id = context.iterator.nextDoc();
			Document doc = context.searcher.doc(id, fnames);
			SolrDocument sdoc = ResponseWriterUtil.toSolrDocument(doc, req.getSchema());
			if (transformer != null) {
				transformer.transform(sdoc, id);
			}
			returnList.add(sdoc);
		}
		if (transformer != null) {
			transformer.setContext(null);
		}

		return returnList;
	}

}
