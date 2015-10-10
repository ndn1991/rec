/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.query.frontend;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.adr.bigdata.search.handler.utils.Constant;
import com.google.common.base.Strings;

/**
 *
 * @author Tong Hoang Anh
 */
public class SuggestionQueryBuilder extends QueryBuilder {
	private static final String QF_PARAM = "product_item_name^50.0 brand_name merchant_name^0.02 category_tree attribute_search";
	private static final String PF_PARAM = "product_item_name^100.0 brand_name merchant_name^0.02 category_tree attribute_search";

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		SuggestionQuery suggestionQuery = SuggestionQuery.createQuery(request);
		solrParams.set(CommonParams.Q, suggestionQuery.getKeyword());
		solrParams.set(CommonParams.ROWS, suggestionQuery.getSuggestionSize());
		solrParams.set(SuggestionQuery.Params.TRAIN, suggestionQuery.getTrain());
		solrParams.add(CommonParams.FQ, "{!collapse field=untokenized_product_item_name}");
		String districtId = suggestionQuery.getDistrictId();
		if (!Strings.isNullOrEmpty(districtId))
			solrParams.add(CommonParams.FQ, "served_district_ids:" + districtId
					+ " OR served_district_ids:0 OR served_district_ids: " + suggestionQuery.getCityId() + "_0");

		String cityId = suggestionQuery.getCityId();
		if (!Strings.isNullOrEmpty(cityId)) {
			solrParams.add(CommonParams.FQ, "received_city_id:" + cityId);
			solrParams.add(CommonParams.FQ, "served_province_ids:" + cityId + " OR (served_province_ids:0)");
		}
		enableFacet(solrParams);
		useDismax(suggestionQuery.getKeyword(), solrParams);
		return solrParams;
	}

	/**
	 * @author minhvv2
	 * @param solrParams
	 */
	private void enableFacet(ModifiableSolrParams solrParams) {
		solrParams.set("facet", "true");
		solrParams.set("facet.mincount", "1");
		solrParams.set("facet.sort", "count");
		solrParams.add("facet.field", "category_id_facet");
		solrParams.set("f.category_id_facet.facet.limit", "10");
	}

	/**
	 * @author minhvv2
	 */
	private void useDismax(String keyword, ModifiableSolrParams solrParams) {
		solrParams.add("defType", Constant.AUTO_PHRASE);
		solrParams.add("defType", Constant.EDISMAX);
		solrParams.set("qf", QF_PARAM);
		solrParams.set("pf", PF_PARAM);
		//		solrParams.set("mm", KeywordAccuracyGenerator.getInstance().GetAccuracyFromKey(keyword));
	}
}
