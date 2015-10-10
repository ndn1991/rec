/**
 * 
 */
package com.adr.bigdata.search.handler.query.frontend;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.firstclassfunction.Function;
import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.adr.bigdata.search.handler.utils.SolrQueryRequestHelper;
import com.google.api.client.util.Strings;
import com.google.common.base.Joiner;

/**
 * @author minhvv2
 *
 */
public class ComboSearchQueryBuilder extends QueryBuilder {
	private Function<String, String> transformer = new Function<String, String>() {
		@Override
		public String apply(String t) {
			return t;
		}
	};

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		//		String cityId = request.getParams().get("cityid");
		//		String districtId = request.getParams().get("districtid");
		//		buildCityFilter(solrParams, cityId);
		//		buildDistrictFilter(solrParams, cityId, districtId);
		String fqQuery = Joiner.on(" OR product_id:").join(
				SolrQueryRequestHelper.extractListParams(request, "productIds", ",", transformer));
		if (!Strings.isNullOrEmpty(fqQuery)) {
			solrParams.add(CommonParams.FQ, "product_id:" + fqQuery);
		}
		//		collapseScore(solrParams, cityId);
		return solrParams;
	}

	private void buildCityFilter(ModifiableSolrParams solrParams, String cityId) {
		if (!Strings.isNullOrEmpty(cityId)) {
			solrParams.add(CommonParams.FQ, "received_city_id:" + cityId);
			solrParams.add(CommonParams.FQ, "served_province_ids:" + cityId + " OR served_province_ids:0");
		}
	}

	private void buildDistrictFilter(ModifiableSolrParams solrParams, String cityId, String districtId) {
		if (!Strings.isNullOrEmpty(districtId)) {
			solrParams.add(CommonParams.FQ, "served_district_ids:" + districtId
					+ " OR served_district_ids:0 OR served_district_ids: " + cityId + "_0");
		}
	}

	private void collapseScore(ModifiableSolrParams solrParams, String cityId) {
		String defaultScore = System.getProperty("solr.boostcity.minscore", "-10000000");
		String boostCityField = Strings.isNullOrEmpty(cityId) ? "city_0_score" : "city_" + cityId + "_score";
		solrParams.add(CommonParams.FQ, boostCityField + ":[0 TO *]");
		String finalScore = String.format("sum(product(boost_score,1000000),def(%s,%s))", boostCityField, defaultScore);
		solrParams.add("collapScore", finalScore);
		String collapse = "{!collapse field=product_item_group max=$collapScore}";
		solrParams.add(CommonParams.FQ, collapse);
	}

}
