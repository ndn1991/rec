/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.getfilter.bean.AbstractQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.bean.AttributeFilterBean;
import com.google.api.client.util.Strings;

/**
 * @author minhvv2
 *
 */
public class AttFilterQueryFactory extends QueryFactory {
	private final static String FILTER = "filter";

	/**
	 * create attribute filter query from request. The filter is in the form of:
	 * id1--value--value::id2--value
	 */
	@Override
	public AbstractQueryBean create(SolrQueryRequest request) {
		String filter = request.getParams().get(FILTER);
		if (Strings.isNullOrEmpty(filter)) {
			return null;
		}
		AttributeFilterBean attBean = new AttributeFilterBean();
		Map<Integer, List<String>> attId2Filter = new HashMap<Integer, List<String>>();
		String[] splitedFilter = filter.trim().split("::");
		for (String elem : splitedFilter) {
			String[] attValues = elem.split("--");
			if (attValues.length >= 2) {
				Integer attId = Integer.valueOf(attValues[0].trim());
				List<String> listFilter = new ArrayList<String>();
				for (int i = 1; i < attValues.length; i++) {
					listFilter.add(attValues[i]);
				}
				attId2Filter.put(attId, listFilter);
			}
		}
		attBean.setAttId2Filter(attId2Filter);
		return attBean;
	}

}
