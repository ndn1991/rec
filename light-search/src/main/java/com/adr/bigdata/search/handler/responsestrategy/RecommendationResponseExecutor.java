/**
 * 
 */
package com.adr.bigdata.search.handler.responsestrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.IntField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResponseWriterUtil;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.TransformContext;
import org.apache.solr.search.DocList;

/**
 * @author minhvv2
 *
 */
public class RecommendationResponseExecutor implements ResponseStrategy {

	@Override
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		// no thing to do
	}

	public void writeRecommendResult(SolrQueryResponse response, SolrDocumentList resultBean) {
		List<NamedList> listProduct = new ArrayList<NamedList>();
		for (SolrDocument solrDoc : resultBean) {
			NamedList bean = new SimpleOrderedMap();
			bean.add("productid", ((IntField) solrDoc.get("product_id")).numericValue().intValue());
			bean.add("productitemid", ((IntField) solrDoc.get("product_item_id")).numericValue().intValue());
			bean.add("merchant_id", ((IntField) solrDoc.get("merchant_id")).numericValue().intValue());
			bean.add("category_id", ((IntField) solrDoc.get("category_id")).numericValue().intValue());
			bean.add("warehouse_id", ((IntField) solrDoc.get("warehouse_id")).numericValue().intValue());
			bean.add("product_item_name", solrDoc.get("product_item_name"));
			listProduct.add(bean);
		}
		response.add("listProduct", listProduct);
	}

	public List<String> extractRecommendProductIds(SolrQueryResponse recommendResponse, SolrQueryRequest req) {
		ResultContext response = (ResultContext) recommendResponse.getValues().get("response");
		recommendResponse.getValues().remove("response");
		List<String> result = new ArrayList<String>();
		if (response != null) {
			/*
			 * response: { numFound: 0, start: 0, docs: [ ] }
			 */
			try {
				TransformContext context = new TransformContext();
				context.req = req;
				context.searcher = req.getSearcher();
				DocList docs = response.docs;
				context.iterator = docs.iterator();

				int sz = docs.size();
				Set<String> fnames = recommendResponse.getReturnFields().getLuceneFieldNames();
				for (int i = 0; i < sz; i++) {
					int id = context.iterator.nextDoc();
					Document doc = context.searcher.doc(id, fnames);
					String[] rec = doc.getValues("recs");
					for (int j = 0; j < rec.length; j++) {
						result.add(extractProductId(rec[j]));
					}
				}
				return result;
			} catch (IOException e) {
				getLogger().error("error reading solr document....{}", e.getStackTrace());
				return result;
			}

		} else {
			return result;
		}

	}

	public SolrDocumentList getSolrDocumentList(SolrQueryResponse rsp, SolrQueryRequest req) {
		ResultContext resultContext = (ResultContext) rsp.getValues().get("response");
		rsp.getValues().remove("response");
		SolrDocumentList returnList = new SolrDocumentList();
		if (resultContext != null) {
			try {
				DocList docs = resultContext.docs;

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
			} catch (IOException ex) {
				getLogger().error("error getting solr document list....", ex.getStackTrace());
				return returnList;
			}
		} else {
			return returnList;
		}
	}

	/*
	 * inputformat: 21504_0.13801311186847084
	 */
	private String extractProductId(String input) {
		return input.split("_", 2)[0];
	}
}
