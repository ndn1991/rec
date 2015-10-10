package com.adr.bigdata.search.handler.query.frontend;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.adr.bigdata.search.handler.utils.StringUtils;
import com.google.common.base.Strings;

public class RelatedProductQueryBuilder extends QueryBuilder {

	private final static String DEFAULT_START = "0";
	private final static String DEFAULT_ROWS = "20";

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		String keyword = request.getParams().get("productname");
		String cityId = request.getParams().get("cityid");
		String districtId = request.getParams().get("districtid");
		keyword = StringUtils.nomalizationStringSearch(keyword);

		solrParams.set(CommonParams.Q, keyword);
		solrParams.set(CommonParams.START, DEFAULT_START);
		solrParams.set(CommonParams.ROWS, DEFAULT_ROWS);
		collapse(solrParams, cityId);
		setCityFilter(solrParams, cityId);
		setDistrictFilter(solrParams, cityId, districtId);
		return solrParams;
	}

	private void collapse(ModifiableSolrParams solrParams, String cityId) {
		String defaultScore = System.getProperty("solr.boostcity.minscore", "-10000000");
		String boostCityField = Strings.isNullOrEmpty(cityId) ? "city_0_score" : "city_" + cityId + "_score";
		solrParams.add(CommonParams.FQ, boostCityField + ":[0 TO *]");
		String finalScore = String.format("sum(product(boost_score,1000000),def(%s,%s))", boostCityField, defaultScore);
		solrParams.add("collapScore", finalScore);
		String collapse = "{!collapse field=product_item_group max=$collapScore}";
		solrParams.add(CommonParams.FQ, collapse);
	}

	private void setCityFilter(ModifiableSolrParams solrParams, String strCityId) {
		if (!Strings.isNullOrEmpty(strCityId)) {
			solrParams.add(CommonParams.FQ, "received_city_id:" + strCityId);
			solrParams.add(CommonParams.FQ, "served_province_ids:" + strCityId + " OR (served_province_ids:0)");
		}
	}

	private void setDistrictFilter(ModifiableSolrParams solrParams, String cityId, String strDistrictId) {
		if (Strings.isNullOrEmpty(cityId) || Strings.isNullOrEmpty(strDistrictId)) {
			// ignoring the case
			return;
		}
		solrParams.add(CommonParams.FQ, "served_district_ids:" + strDistrictId
				+ " OR served_district_ids:0 OR (served_district_ids: " + cityId + "_0)");
	}

}
