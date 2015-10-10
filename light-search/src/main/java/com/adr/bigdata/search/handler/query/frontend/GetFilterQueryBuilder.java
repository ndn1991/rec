package com.adr.bigdata.search.handler.query.frontend;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.query.AttributeFilter;
import com.adr.bigdata.search.handler.utils.EdisMaxUtils;
import com.adr.bigdata.search.handler.utils.SolrParamHelper;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.nhb.common.Loggable;

public class GetFilterQueryBuilder implements Loggable {
	private static final String MERCHANT_PROMOTION_ID = System.getProperty("promotion.policy.merchantid", "2282");// vinmart special promotion policy
	private static final String PRODUCT_ITEM_TYPE_PROMOTION = System.getProperty(
			"promotion.policy.exclude.productitemtype", "4");// vinmart special promotion policy
	private static final String PROMOTION_QUERY = "(is_promotion_mapping:true AND is_promotion:true AND start_time_discount:[ * TO %d] AND finish_time_discount:[%d TO *]) OR (price_flag:true AND -product_item_type:%s AND merchant_id:%s) OR (is_not_apply_commision:true)";
	private static final String PRICE_LOCAL_PARAM = "if(is_not_apply_commision,product(if(and(and(and(max(sub(ms(NOW), start_time_discount), 0), max(sub(finish_time_discount,ms(NOW)), 0)), is_promotion),is_promotion_mapping), promotion_price, sell_price),sub(1,div(commision_fee,100))),if(and(and(and(max(sub(ms(NOW), start_time_discount), 0), max(sub(finish_time_discount,ms(NOW)), 0)), is_promotion),is_promotion_mapping), promotion_price, sell_price))";
	private static final long IS_NEW_TIME_WINDOWS = 604800000;// 7 days
	private final AttributeFilterQueryBuilder attrFilterBuider;

	public GetFilterQueryBuilder() {
		attrFilterBuider = new AttributeFilterQueryBuilder();
	}

	public SolrParams buildSolrQuery(SolrQueryRequest request, SolrQueryResponse response, GetFilterQuery query) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		buildFacetParams(solrParams, query);

		if (query.isSearchByCat() || query.isSearchByCatFilterId()) {
			try {
				String catIdForAttributeFilter = Strings.isNullOrEmpty(query.getFilteredCategoryId()) ? query
						.getCateogryId() : query.getFilteredCategoryId();

				attrFilterBuider.buildAttributeFacet(solrParams, catIdForAttributeFilter, response);

			} catch (Exception e) {
				getLogger().error("exception building attribute filter...." + e.getMessage());
			}
		}

		buildMultiMapParams(solrParams, query);
		EdisMaxUtils.useDismax(query, solrParams);
		return solrParams;
	}

	public SolrParams buildCachedAttributeQuery(SolrQueryRequest request, SolrQueryResponse response,
			GetFilterQuery query, Object cachedAtt) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		buildFacetParams(solrParams, query);

		if (query.isSearchByCat() || query.isSearchByCatFilterId()) {
			try {
				attrFilterBuider.buildCachedFacet(solrParams, (Map) cachedAtt, response);
			} catch (Exception e) {
				getLogger().error("exception building attribute filter...." + e.getMessage());
			}
		}
		buildMultiMapParams(solrParams, query);
		EdisMaxUtils.useDismax(query, solrParams);
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

	private String[] buildFeaturedFacetQuery() {
		long now = System.currentTimeMillis();
		String promotionQuery = String.format(PROMOTION_QUERY, now, now, PRODUCT_ITEM_TYPE_PROMOTION,
				MERCHANT_PROMOTION_ID);
		String isNewQuery = "create_time:[ " + String.valueOf(System.currentTimeMillis() - IS_NEW_TIME_WINDOWS)
				+ " TO *]";
		return new String[] { promotionQuery, isNewQuery };
	}

	private void buildFacetParams(ModifiableSolrParams solrParams, GetFilterQuery getFilterQuery) {
		solrParams.set(CommonParams.Q, getFilterQuery.getKeyword());
		solrParams.set("facet", "true");
		solrParams.set("facet.mincount", "1");
		solrParams.set("facet.sort", "count");
		solrParams.set("facet.thread", "-1");

		solrParams.set("facet.query", buildFeaturedFacetQuery());
		solrParams.set("stats", "true");
		solrParams.set("stats.field", "sell_price");
		solrParams.set("rows", "0");

		solrParams.add("facet.field", "brand_id_facet");
		solrParams.add("facet.field", "category_id_facet");
		solrParams.add("facet.field", "merchant_id_facet");
		solrParams.set("f.category_id_facet.facet.limit", "10");
		// remove merchant, brand, attribute filter limit since sprint 6. The default value of SOLR is 100
		//		solrParams.set("f.merchant_id_facet.facet.limit", "20");
	}

	private void buildMultiMapParams(ModifiableSolrParams solrParams, GetFilterQuery getFilterQuery) {
		List<String> filter = new ArrayList<>();
		setBrandFilter(getFilterQuery, filter, solrParams);
		setCityFilter(getFilterQuery, filter);
		setDistrictFilter(getFilterQuery, filter);
		setCategoryFilter(getFilterQuery, filter, solrParams);
		setPriceFilter(getFilterQuery, filter, solrParams);
		setFeaturedFilter(getFilterQuery, filter, solrParams);
		setMerchantFilter(getFilterQuery, filter, solrParams);
		setAttributeFilter(getFilterQuery, filter, solrParams);
		String[] filterArr = new String[filter.size()];
		filterArr = filter.toArray(filterArr);
		if (filterArr.length != 0) {
			solrParams.add(CommonParams.FQ, filterArr);
		}
		buildCollapse(solrParams, getFilterQuery.getCityId());
	}

	private void setCityFilter(GetFilterQuery getFilterQuery, List<String> filter) {
		String strCityId = getFilterQuery.getCityId();
		if (!Strings.isNullOrEmpty(strCityId)) {
			int cityId = -1;
			try {
				cityId = Integer.parseInt(strCityId);
			} catch (Exception e) {
				cityId = -1;
			}

			if (cityId > 0) {
				filter.add("received_city_id:" + strCityId);
				filter.add("served_province_ids:" + strCityId + " OR (served_province_ids:0)");
			}

		}

	}

	private void setDistrictFilter(GetFilterQuery getFilterQuery, List<String> filter) {
		String cityId = getFilterQuery.getCityId();
		String strDistrictId = getFilterQuery.getDistrictId();
		if (Strings.isNullOrEmpty(cityId) || Strings.isNullOrEmpty(strDistrictId)) {
			// ignoring the case
			return;
		}

		int districtId = -1;
		try {
			districtId = Integer.parseInt(strDistrictId);
		} catch (Exception e) {
			districtId = -1;
		}

		if (districtId > 0) {
			filter.add("served_district_ids:" + strDistrictId + " OR served_district_ids:0 OR (served_district_ids: "
					+ cityId + "_0)");
		}

	}

	private void setBrandFilter(GetFilterQuery getFilterQuery, List<String> filter, ModifiableSolrParams solrParams) {
		if (getFilterQuery.getBrandIds() != null && !getFilterQuery.getBrandIds().isEmpty()) {
			Stream<String> brands = getFilterQuery.getBrandIds().stream().map(brandId -> "brand_id:" + brandId);
			if (getFilterQuery.getIsGetBrand() != 1) {
				filter.add("{!tag=brand}" + Joiner.on(" OR ").join(brands.iterator()));
				SolrParamHelper.writeTaggedFacet(solrParams, "brand_id_facet", "{!ex=brand}brand_id_facet");
			} else {
				filter.add(Joiner.on(" OR ").join(brands.iterator()));
				solrParams.remove("facet.field", "brand_id_facet");
			}
		}
	}

	private void setCategoryFilter(GetFilterQuery getFilterQuery, List<String> filter, ModifiableSolrParams solrParams) {
		if (!Strings.isNullOrEmpty(getFilterQuery.getCateogryId())
				|| !Strings.isNullOrEmpty(getFilterQuery.getFilteredCategoryId())) {
			int categoryId = -1;
			int filteredCategoryId = -1;
			try {
				categoryId = Integer.parseInt(getFilterQuery.getCateogryId());
			} catch (Exception e) {
				categoryId = -1;
			}
			try {
				filteredCategoryId = Integer.parseInt(getFilterQuery.getFilteredCategoryId());
			} catch (Exception e) {
				filteredCategoryId = -1;
			}
			if (filteredCategoryId > 0) {
				filter.add("{!tag=cat}category_id:" + getFilterQuery.getFilteredCategoryId());
			} else if (categoryId > 0) {
				filter.add("{!tag=cat}category_path:" + getFilterQuery.getCateogryId());
			}
			SolrParamHelper.writeTaggedFacet(solrParams, "category_id_facet", "{!ex=cat}category_id_facet");
		}
	}

	private void setPriceFilter(GetFilterQuery getFilterQuery, List<String> filter, ModifiableSolrParams solrParams) {
		List<String> priceRange = getFilterQuery.getPrices();
		if (priceRange != null && !priceRange.isEmpty()) {
			solrParams.set("price", PRICE_LOCAL_PARAM);
			for (String s : priceRange) {
				if (s.contains("TO")) {
					String[] splitted = s.split("TO");
					if (splitted.length == 2) {
						String lower = splitted[0];
						String upper = splitted[1];
						//$price already define in collapse
						String priceFilter = String.format("{!frange l=%s u=%s}$price", lower, upper);
						filter.add(priceFilter);
						return;
					}
				} else {
					String lower = s;
					String upper = s;
					String priceFilter = String.format("{!frange l=%s u=%s}$price", lower, upper);
					filter.add(priceFilter);
					return;
				}
			}
		}
	}

	private void setAttributeFilter(GetFilterQuery getFilterQuery, List<String> filter, ModifiableSolrParams solrParams) {
		Set<AttributeFilter> attributeFilters = getFilterQuery.getAttributeFilters();
		if (attributeFilters != null && !attributeFilters.isEmpty()) {
			attributeFilters.stream().forEach(
					(attributeFilter) -> {
						String attributeField = attributeFilter.toAttributeField();
						SolrParamHelper.writeTaggedFacet(solrParams, attributeField, "{!ex=" + attributeField + "}"
								+ attributeField);
						filter.add(attributeFilter.getFilterQuery());
					});
		}
	}

	private void setFeaturedFilter(GetFilterQuery query, List<String> filter, ModifiableSolrParams solrParams) {
		String isPromotion = query.getIsPromotion();
		String isNew = query.getIsNew();
		// both
		String[] featuredFilter = buildFeaturedFacetQuery();
		String tag = "{!tag=featured}";
		if (!Strings.isNullOrEmpty(isPromotion) && isPromotion.equals("true") && !Strings.isNullOrEmpty(isNew)
				&& isNew.equals("true")) {
			filter.add("(" + featuredFilter[0] + ") OR " + featuredFilter[1]);
			return;
		} else if (!Strings.isNullOrEmpty(isPromotion) && isPromotion.equals("true")) {
			filter.add(tag + featuredFilter[0]);
			setExcludeFeatured(solrParams);
			return;
		} else if (!Strings.isNullOrEmpty(isNew) && isNew.equals("true")) {
			filter.add(tag + featuredFilter[1]);
			setExcludeFeatured(solrParams);
			return;
		}

	}

	private void setExcludeFeatured(ModifiableSolrParams solrParams) {
		solrParams.remove("facet.query");
		long now = System.currentTimeMillis();
		String isNewQuery = "{!ex=featured}create_time:[ "
				+ String.valueOf(System.currentTimeMillis() - IS_NEW_TIME_WINDOWS) + " TO *]";
		String promotionQuery = "{!ex=featured}"
				+ String.format(PROMOTION_QUERY, now, now, PRODUCT_ITEM_TYPE_PROMOTION, MERCHANT_PROMOTION_ID);
		solrParams.set("facet.query", promotionQuery, isNewQuery);
	}

	private void setMerchantFilter(GetFilterQuery getFilterQuery, List<String> filter, ModifiableSolrParams solrParams) {
		String merchantId = getFilterQuery.getMerchantId();
		if (!Strings.isNullOrEmpty(merchantId)) {
			Stream<String> lstMerchants = getFilterQuery.getLstMerchantFilters().stream()
					.map(merchant -> "merchant_id:" + merchant);
			if (!getFilterQuery.isGetMerchant()) {
				filter.add("{!tag=merchant}" + Joiner.on(" OR ").join(lstMerchants.iterator()));
				SolrParamHelper.writeTaggedFacet(solrParams, "merchant_id_facet", "{!ex=merchant}merchant_id_facet");
			} else {
				filter.add(Joiner.on(" OR ").join(lstMerchants.iterator()));
				solrParams.remove("facet.field", "merchant_id_facet");
			}
		}
	}

}
