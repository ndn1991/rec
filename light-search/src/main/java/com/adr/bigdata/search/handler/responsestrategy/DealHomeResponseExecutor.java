package com.adr.bigdata.search.handler.responsestrategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocSlice;

import com.adr.bigdata.search.handler.query.deal.DealFields;

public class DealHomeResponseExecutor implements ResponseStrategy {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		SimpleOrderedMap grouped = (SimpleOrderedMap) rsp.getValues().get("grouped");
		SimpleOrderedMap cat1 = (SimpleOrderedMap) grouped.get(DealFields.CAT_1);
		List<SimpleOrderedMap> groups = (List<SimpleOrderedMap>) cat1.get("groups");
		rsp.getValues().remove("grouped");

		List<SimpleOrderedMap> list = new ArrayList<SimpleOrderedMap>();
		for (SimpleOrderedMap element : groups) {
			int cat1Id = (Integer) element.getVal(0);
			DocSlice docList = (DocSlice) element.getVal(1);

			SimpleOrderedMap resultElement = new SimpleOrderedMap();
			resultElement.add("categoryId", cat1Id);
			resultElement.add("list", docList);

			list.add(resultElement);
		}
		
		rsp.getValues().add("list", list);
	}
}
