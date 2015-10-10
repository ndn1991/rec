package com.adr.bigdata.search.handler.responsestrategy;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

public class CustomerServiceResponseExecutor implements ResponseStrategy {

	@Override
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		SimpleOrderedMap<?> spMap = (SimpleOrderedMap<?>) rsp.getValues().get("grouped");
		rsp.getValues().remove("grouped");
		if (spMap != null) {
			/*
			 * JSON FORMAT grouped: { product_item_group: { matches: 43,
			 * ngroups: 20, doclist: { numFound: 43, start: 0, docs: [
			 */
			//			getLogger().info("customer service....spMap type " + spMap.getClass());
			//			getLogger().info("customer service....spMap content " + spMap.toString());
			SimpleOrderedMap<?> spMapResult = (SimpleOrderedMap<?>) spMap.getVal(0);

			rsp.add("listProductitemMerchant", spMapResult.getVal(1));
			rsp.add("numFound", spMapResult.getVal(0));
		} else {
			rsp.add("listProductitemMerchant", new NamedList());
			rsp.add("numFound", 0);
		}
	}

}
