package com.adr.bigdata.search.handler.query.frontend;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.google.common.base.Strings;

public class CustomerServiceQuery {
	private final static String CS_PATTERN_ID = "^(id:)(.+)$";
	private final static String CS_PATTERN_SKU = "^(sku:)(.+)$";
	private final static String CS_PATTERN_BARCODE = "^(bar:)(.+)$";
	private final static String BARCODE = "barcode:";
	private final static String SKU = "merchant_product_item_sku:";
	private final static String DEFAUL_KEYWORD = "*:*";
	private final static String PRODUCTID = "product_id:";
	private final static String PRODUCT_ITEM_ID = "product_item_id:";

	public CustomerServiceQuery(SolrQueryRequest request) {
		SolrParams param = request.getParams();
		this.keyword = DEFAUL_KEYWORD;
		makeupKeyword(param.get(Params.KEYWORD));
		start = param.get(Params.START);
		rows = param.get(Params.ROWS);
	}

	private void makeupKeyword(String rawKeyword) {
		filterQueries = new ArrayList<String>();
		if (!Strings.isNullOrEmpty(rawKeyword)) {
			rawKeyword = rawKeyword.trim();
			if (rawKeyword.matches(CS_PATTERN_BARCODE)) {
				String barcode = rawKeyword.replaceAll(CS_PATTERN_BARCODE, "$2");
				filterQueries.add(BARCODE + barcode);
			} else if (rawKeyword.matches(CS_PATTERN_SKU)) {
				String sku = rawKeyword.replaceAll(CS_PATTERN_SKU, "$2");
				filterQueries.add(SKU + sku);
			} else if (rawKeyword.matches(CS_PATTERN_ID)) {
				String id = rawKeyword.replaceAll(CS_PATTERN_ID, "$2");
				filterQueries.add(PRODUCTID + id + " OR " + PRODUCT_ITEM_ID + id);
			} else {
				this.keyword = rawKeyword;
			}
		}
	}

	private List<String> filterQueries;

	public String[] getFilterQuery() {
		return filterQueries.toArray(new String[filterQueries.size()]);
	}

	private final String start;
	private final String rows;
	private String keyword;

	public String getKeyword() {
		return keyword;
	}

	public String getStart() {
		return start;
	}

	public String getRows() {
		return rows;
	}

	static class Params {
		final static String KEYWORD = "keyword";
		final static String START = "start";
		final static String ROWS = "rows";
	}
}
