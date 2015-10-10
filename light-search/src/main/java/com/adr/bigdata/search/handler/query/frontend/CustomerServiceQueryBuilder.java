package com.adr.bigdata.search.handler.query.frontend;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.google.common.base.Strings;
import com.nhb.common.Loggable;

public class CustomerServiceQueryBuilder extends FrontEndQueryBuilder implements Loggable {

	private final static String CS_PATTERN_ID = "^(id:)(.+)$";
	private final static String CS_PATTERN_SKU = "^(sku:)(.+)$";
	private final static String CS_PATTERN_BARCODE = "^(bar:)(.+)$";
	private final static String BARCODE = "barcode:";
	private final static String SKU = "merchant_product_item_sku:";
	private final static String PRODUCTID = "product_id:";
	private final static String PRODUCT_ITEM_ID = "product_item_id:";
	private final static String DEFAULT_KEYWORD = "*:*";

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		ModifiableSolrParams solrParams = (ModifiableSolrParams) super.buildSolrQuery(request);
		String keyword = request.getParams().get("keyword");
		if (isPatternQuery(keyword)) {
			solrParams.set(CommonParams.Q, DEFAULT_KEYWORD);
			solrParams.add(CommonParams.FQ, builFilterFromKeyword(keyword));

			solrParams.remove("defType");
			solrParams.remove("mm");
			solrParams.remove("qf");
			solrParams.remove("pf");
		}

		return solrParams;
	}

	/*
	 * the rawkeyword must not null or empty
	 */
	private String builFilterFromKeyword(String rawKeyword) {
		rawKeyword = rawKeyword.trim();
		if (rawKeyword.matches(CS_PATTERN_BARCODE)) {
			String barcode = rawKeyword.replaceAll(CS_PATTERN_BARCODE, "$2");
			return BARCODE + barcode;
		} else if (rawKeyword.matches(CS_PATTERN_SKU)) {
			String sku = rawKeyword.replaceAll(CS_PATTERN_SKU, "$2");
			return SKU + sku;
		} else if (rawKeyword.matches(CS_PATTERN_ID)) {
			String id = rawKeyword.replaceAll(CS_PATTERN_ID, "$2");
			return PRODUCTID + id + " OR " + PRODUCT_ITEM_ID + id;
		}
		return null;

	}

	private boolean isPatternQuery(String rawKeyword) {
		return (!Strings.isNullOrEmpty(rawKeyword))
				&& (rawKeyword.matches(CS_PATTERN_BARCODE) || rawKeyword.matches(CS_PATTERN_SKU) || rawKeyword
						.matches(CS_PATTERN_ID));
	}

}
