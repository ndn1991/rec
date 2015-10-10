/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.factory;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.getfilter.bean.AbstractQueryBean;
import com.google.api.client.util.Strings;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
public abstract class QueryFactory implements Loggable {
	protected final static String CITY_ID = "cityid";
	protected final static String CAT_ID = "catid";
	protected final static String CAT_TYPE = "cattype";
	protected final static String MERCHANT_ID = "merchantid";
	protected final static String IS_PROMOTION = "ispromotion";
	protected final static String BRAND_ID = "brandid";
	protected final static String PRICE = "price";
	protected final static String IS_NEW = "isnew";
	protected final static String KEYWORD = "keyword";

	public abstract AbstractQueryBean create(SolrQueryRequest request);

	protected boolean tryParseBooleanVal(String input) {
		if (Strings.isNullOrEmpty(input)) {
			return false;
		}
		return Boolean.valueOf(input);
	}

	protected List<Integer> tryParseListString(String input) {
		if (Strings.isNullOrEmpty(input)) {
			return null;
		}
		List<Integer> result = new ArrayList<Integer>();
		String[] splittedStr = input.split("--");
		for (String elem : splittedStr) {
			try {
				Integer elemVal = Integer.valueOf(elem);
				result.add(elemVal);
			} catch (NumberFormatException e) {
				//Do nothing, just ignore this one
				getLogger().error("wrong param format at {}...{}", this.getClass(), e.getMessage());
			}
		}
		return result;
	}
}