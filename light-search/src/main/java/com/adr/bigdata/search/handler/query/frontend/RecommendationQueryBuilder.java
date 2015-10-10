/**
 * 
 */
package com.adr.bigdata.search.handler.query.frontend;

import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;

/**
 * @author minhvv2
 *
 */
public class RecommendationQueryBuilder extends QueryBuilder {

	public SolrParams buildRecommendQuery(String productId) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		String[] listProductIds = productId.split(",");
		String fqQuery = Joiner.on(" OR product_id: ").join(Arrays.asList(listProductIds));
		solrParams.add(CommonParams.FQ, "product_id:" + fqQuery);
		return solrParams;
	}

	public SolrParams setJsonWriter() {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.set("wt", "groupedjson");
		solrParams.set("omitHeader", "true");
		return solrParams;
	}

	public SolrParams buildComplementQuery(String productId, String catId, String cityId, String districtId,
			int complementRows) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		solrParams.add(CommonParams.FQ, "category_id:" + catId);
		solrParams.add(CommonParams.FQ, "-product_id:" + productId);
		buildCityDistrictQuery(solrParams, cityId, districtId);
		collapse(solrParams, cityId);
		solrParams.add(CommonParams.ROWS, String.valueOf(complementRows));
		return solrParams;
	}

	public SolrParams buildQueryFromRecommendedIds(List<String> recommendedProductIds, String cityId, String districtId) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		String fq = Joiner.on(" OR product_id: ").join(recommendedProductIds);
		solrParams.add(CommonParams.FQ, "product_id:" + fq);
		buildCityDistrictQuery(solrParams, cityId, districtId);
		collapse(solrParams, cityId);
		return solrParams;
	}

	private void buildCityDistrictQuery(ModifiableSolrParams solrParams, String cityId, String districtId) {
		solrParams.add(CommonParams.FQ, "received_city_id:" + cityId);
		solrParams.add(CommonParams.FQ, "served_province_ids:" + cityId + " OR (served_province_ids:0)");
		solrParams.add(CommonParams.FQ, "served_district_ids:" + districtId
				+ " OR served_district_ids:0 OR (served_district_ids: " + cityId + "_0)");
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

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		String keyword = request.getParams().get("productname");
		String productId = request.getParams().get("productid");
		String cityId = request.getParams().get("cityid");
		String districtId = request.getParams().get("districtid");
		solrParams.add(CommonParams.FQ, "product_id:" + productId);
		return solrParams;
	}
}
