package com.adr.bigdata.search.handler.query.itemneigh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryRequestBase;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.DocIterator;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.ndn.expression.tree.Tree;
import com.ndn.expression.tree.ValuedTree;
import com.nhb.common.Loggable;

public class ItemNeighsQuery implements Loggable {
	private int limit = 24;
	private String sortyBy = null;
	private String order = null;
	private String cityId = null;
	private String districtId = null;
	private int productId = -1;
	private SolrCore solrCore;
	private SolrQueryRequest request;
	private Map<Tree, String> ruleSet;

	public ItemNeighsQuery(SolrQueryRequest req, Map<Tree, String> ruleSet) {
		SolrParams params = req.getParams();
		this.limit = params.getInt(ItemNeighsParams.LIMIT, 3);
		this.productId = Integer.parseInt(params.get(ItemNeighsParams.PRODUCT_ID));
		this.solrCore = req.getCore();
		this.request = req;
		this.ruleSet = ruleSet;

		this.sortyBy = params.get(ItemNeighsParams.SORT_BY);
		if (this.sortyBy != null && !this.sortyBy.trim().isEmpty()) {
			this.order = params.get(ItemNeighsParams.ORDER);
			if (this.order == null || this.order.trim().isEmpty()) {
				throw new IllegalArgumentException("sortby is not null but order is null");
			}
			this.sortyBy = this.sortyBy.trim();
			this.order = this.order.trim();
		}

		String sCityId = params.get(ItemNeighsParams.CITY_ID);
		if (sCityId != null && !sCityId.isEmpty()) {
			this.cityId = sCityId;
			getLogger().debug("cityId: " + this.cityId);
		} else {
			getLogger().debug("cityId is null");
		}

		String sDistrictId = params.get(ItemNeighsParams.DISTRICT_ID);
		if (sDistrictId != null && !sDistrictId.isEmpty()) {
			this.districtId = sDistrictId;
			getLogger().debug("districtId: " + this.districtId);
		} else {
			getLogger().debug("districtIds is null");
		}
	}

	public SolrParams getQueryMap() throws Exception {
		ModifiableSolrParams paramGetProduct = new ModifiableSolrParams();
		paramGetProduct.set(CommonParams.Q, "*:*");
		List<String> filterGetProduct = new ArrayList<String>();
		filterGetProduct.add("product_id:" + productId);
		// collapse(paramGetProduct, filterGetProduct, this.cityId);
		String defaultScore = System.getProperty("solr.boostcity.minscore", "-10000000");
		String boostCityField = Strings.isNullOrEmpty(cityId) ? "city_0_score" : "city_" + this.cityId + "_score";
		String finalScore = String.format("sum(product(boost_score,1000000),def(%s,%s))", boostCityField, defaultScore);
		paramGetProduct.add("collapScore", finalScore);
		String collapse = "{!collapse field=product_item_group max=$collapScore}";
		filterGetProduct.add(collapse);
		if (!filterGetProduct.isEmpty()) {
			String[] arrFilter = new String[filterGetProduct.size()];
			filterGetProduct.toArray(arrFilter);
			paramGetProduct.set(CommonParams.FQ, arrFilter);
		}

		SolrQueryRequest req = new SolrQueryRequestBase(this.solrCore, paramGetProduct) {
		};
		SolrQueryResponse res = new SolrQueryResponse();
		getLogger().error("params: " + paramGetProduct);
		getLogger().error("solrCore: " + this.solrCore);
		getLogger().error("handler: " + this.solrCore.getRequestHandler("/select"));
		this.solrCore.getRequestHandler("/select").handleRequest(req, res);
		ResultContext response = (ResultContext) res.getValues().get("response");
		getLogger().error("result doc: " + response.docs);
		DocIterator it = response.docs.iterator();
		if (it.hasNext()) {
			int id = it.next();
			Document doc = this.request.getSearcher().doc(id);
			Map<String, Object> map = new HashMap<String, Object>();
			doc.getFields().forEach(field -> {
				Object[] os = doc.getValues(field.name());
				if (os != null) {
					if (os.length > 1) {
						List<String> tmpList = new ArrayList<String>();
						for (int i = 0; i < os.length; i++) {
							tmpList.add(os[i].toString());
						}
						map.put(field.name(), tmpList);						
					} else {
						map.put(field.name(), os[0]);	
					}
				}
			});

			getLogger().error("result map: " + map);
			List<String> criteria = getAppropriateRule(map);

			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set(CommonParams.Q, "*:*");
			params.set(CommonParams.ROWS, this.limit);
			if (!criteria.isEmpty()) {
				params.add(CommonParams.FQ, "(" + Joiner.on(") OR (").join(criteria) + ")");
			}

			if (this.sortyBy != null && !this.sortyBy.isEmpty()) {
				params.set(CommonParams.SORT, this.sortyBy + " " + this.order);
			}

			List<String> filters = new ArrayList<String>();
			setCityFilter(this.cityId, filters);
			setDistrictFilter(this.cityId, this.districtId, filters);
			collapse(params, filters, this.cityId);

			if (!filters.isEmpty()) {
				String[] arrFilter = new String[filters.size()];
				filters.toArray(arrFilter);
				params.add(CommonParams.FQ, arrFilter);
			}

			getLogger().error("final params: " + params);
			return params;
		} else {
			throw new RuntimeException("Fuck error");
		}
	}

	private List<String> getAppropriateRule(Map<String, Object> map) {
		List<String> result = new ArrayList<String>();
		getLogger().error("zzz: " + ruleSet);
		ruleSet.keySet().forEach(tree -> {
			ValuedTree vTree = tree.assignValue(map);
			if (vTree.result()) {
				getLogger().error("onetrue");
				result.add(ruleSet.get(tree));
				getLogger().error("result: " + result);
			}
		});
		getLogger().error("result-xzy: " + result);
		return result;
	}

	private void collapse(ModifiableSolrParams solrParams, List<String> filter, String cityId) {
		String defaultScore = System.getProperty("solr.boostcity.minscore", "-10000000");
		String boostCityField = Strings.isNullOrEmpty(cityId) ? "city_0_score" : "city_" + cityId + "_score";
		filter.add(boostCityField + ":[0 TO *]");
		String finalScore = String.format("sum(product(boost_score,1000000),def(%s,%s))", boostCityField, defaultScore);
		solrParams.add("collapScore", finalScore);
		String collapse = "{!collapse field=product_item_group max=$collapScore}";
		filter.add(collapse);
	}

	private void setCityFilter(String cityId, List<String> filter) {
		if (!Strings.isNullOrEmpty(cityId)) {
			filter.add("received_city_id:" + cityId);
			filter.add("served_province_ids:" + cityId + " OR (served_province_ids:0)");
			// filter.add("city_" + cityId + "_score:[0 TO *]");
		}
	}

	private void setDistrictFilter(String cityId, String districtId, List<String> filter) {
		if (!Strings.isNullOrEmpty(districtId) && !Strings.isNullOrEmpty(cityId))
			filter.add("served_district_ids:" + districtId + " OR served_district_ids:0 OR served_district_ids: "
					+ cityId + "_0");
	}
}
