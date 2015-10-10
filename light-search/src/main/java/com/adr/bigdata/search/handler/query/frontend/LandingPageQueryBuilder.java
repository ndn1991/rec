/**
 * 
 */
package com.adr.bigdata.search.handler.query.frontend;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.google.common.base.Strings;

/**
 * @author minhvv2
 *
 */
public class LandingPageQueryBuilder extends QueryBuilder {
	private static final String LANDING_PAGE_ORDER = "landing_page_%s_order";
	private static final String LANDING_PAGE_GROUP_ORDER = "landing_page_group_%s_order";
	private static final String DEFAUL_START = "0";
	private static final String DEFAUL_LIMIT = "24";

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		String cityId = request.getParams().get(Param.CITY_ID.toString());
		String districtId = request.getParams().get(Param.DISTRICT_ID.toString());
		String landingPageId = request.getParams().get(Param.LANDING_PAGE_ID.toString());
		String groupId = request.getParams().get(Param.GROUP_ID.toString());
		String start = request.getParams().get(Param.START.toString());
		String rows = request.getParams().get(Param.ROWS.toString());
		solrParams.set(CommonParams.START, Strings.isNullOrEmpty(start) ? DEFAUL_START : start);
		solrParams.set(CommonParams.ROWS, Strings.isNullOrEmpty(rows) ? DEFAUL_LIMIT : rows);
		buildCollapse(solrParams, cityId);
		buildCityFilter(cityId, solrParams);
		buildDistrictFilter(districtId, cityId, solrParams);
		buildLandingPageFilter(landingPageId, groupId, solrParams);
		return solrParams;
	}

	private void buildCollapse(ModifiableSolrParams solrParams, String cityId) {
		String defaultScore = System.getProperty("solr.boostcity.minscore", "-10000000");
		String boostCityField = Strings.isNullOrEmpty(cityId) ? "city_0_score" : "city_" + cityId + "_score";
		solrParams.add(CommonParams.FQ, boostCityField + ":[0 TO *]");// filter score > 0
		String finalScore = String.format("sum(product(boost_score,1000000),def(%s,%s))", boostCityField, defaultScore);
		solrParams.add("collapScore", finalScore);
		String collapse = "{!collapse field=product_item_group max=$collapScore}";
		solrParams.add(CommonParams.FQ, collapse);
	}

	private void buildLandingPageFilter(String landingPageId, String groupId, ModifiableSolrParams solrParams) {
		assert (!Strings.isNullOrEmpty(landingPageId));
		String landingOrderField = String.format(LANDING_PAGE_ORDER, landingPageId);
		solrParams.add(CommonParams.FQ, landingOrderField + ":*");
		if (Strings.isNullOrEmpty(groupId)) {
			solrParams.add(CommonParams.SORT, landingOrderField + " asc");
		} else {
			String groupOrderField = String.format(LANDING_PAGE_GROUP_ORDER, groupId);
			solrParams.add(CommonParams.FQ, groupOrderField + ":*");
			solrParams.add(CommonParams.SORT, groupOrderField + " asc");
		}
	}

	private void buildCityFilter(String cityId, ModifiableSolrParams solrParams) {
		if (!Strings.isNullOrEmpty(cityId)) {
			solrParams.add(CommonParams.FQ, "received_city_id:" + cityId);
			solrParams.add(CommonParams.FQ, "served_province_ids:" + cityId + " OR (served_province_ids:0)");
			solrParams.add(CommonParams.FQ, "city_" + cityId + "_score:[0 TO *]");
		}
	}

	private void buildDistrictFilter(String districtId, String cityId, ModifiableSolrParams solrParams) {
		if (!Strings.isNullOrEmpty(districtId))
			solrParams.add(CommonParams.FQ, "served_district_ids:" + districtId
					+ " OR served_district_ids:0 OR served_district_ids: " + cityId + "_0");
	}

	enum Param {
		LANDING_PAGE_ID("ldpageid"),
		GROUP_ID("groupid"),
		CITY_ID("cityid"),
		DISTRICT_ID("districtid"),
		START("offset"),
		ROWS("limit");

		private String param;

		private Param(String param) {
			this.param = param;
		}

		@Override
		public String toString() {
			return this.param;
		}

	}
}
