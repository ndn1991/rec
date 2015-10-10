package com.adr.bigdata.search.handler.query.frontend;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.AttributeFilter;
import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.adr.bigdata.search.handler.query.SortQuery;
import com.adr.bigdata.search.handler.query.SortType;
import com.adr.bigdata.search.handler.utils.Constant;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.nhb.common.Loggable;

public class FrontEndQueryBuilder extends QueryBuilder implements Loggable {
	private static final String MERCHANT_PROMOTION_ID = System.getProperty("promotion.policy.merchantid", "2282");// vinmart special promotion policy
	private static final String PRODUCT_ITEM_TYPE_PROMOTION = System.getProperty(
			"promotion.policy.exclude.productitemtype", "4");// vinmart special promotion policy
	private static final String PRICE_LOCAL_PARAM = "if(is_not_apply_commision,product(if(and(and(and(max(sub(ms(NOW), start_time_discount), 0), max(sub(finish_time_discount,ms(NOW)), 0)), is_promotion),is_promotion_mapping), promotion_price, sell_price),sub(1,div(commision_fee,100))),if(and(and(and(max(sub(ms(NOW), start_time_discount), 0), max(sub(finish_time_discount,ms(NOW)), 0)), is_promotion),is_promotion_mapping), promotion_price, sell_price))";
	private static final String PROMOTION_QUERY = "(is_promotion_mapping:true AND is_promotion:true AND start_time_discount:[ * TO %d] AND finish_time_discount:[%d TO *]) OR (price_flag:true AND -product_item_type:%s AND merchant_id:%s) OR (is_not_apply_commision:true)";
	private static final String NEW_QUERY = "create_time:[%d TO *]";
	private static final long SEVEN_DAYS = 604800000l; //in milisecond
	private TrendingKeywordPreprocessor preProcessor = new TrendingKeywordPreprocessor();

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		FrontEndQuery query = FrontEndQuery.createFrontendQuery(request);
		buildMapParams(solrParams, query);
		buildMultiMapParams(solrParams, query);

		preProcessor.preProcessKeyword(solrParams, query.getKeyword());
		getLogger().debug("{Front End Query}: " + solrParams.toString());
		return solrParams;
	}

	private void buildMapParams(ModifiableSolrParams solrParams, FrontEndQuery frontEndQuery) {
		solrParams.set(CommonParams.Q, frontEndQuery.getKeyword());
		solrParams.set(CommonParams.START, frontEndQuery.getStart());
		solrParams.set(CommonParams.ROWS, frontEndQuery.getRows());
		sortQuery(solrParams, frontEndQuery.getSortType(), frontEndQuery.getIsAscending());
		// do not dismax when search by SKU
		if (!frontEndQuery.isSearchBySku()) {
			useDismax(frontEndQuery, solrParams);
		}
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

	private void buildMultiMapParams(ModifiableSolrParams solrParams, FrontEndQuery frontEndQuery) {
		List<String> filter = new ArrayList<>();
		setBrandFilter(frontEndQuery, filter);
		setCategoryFilter(frontEndQuery, filter, solrParams);
		setPriceFilter(frontEndQuery, filter, solrParams);
		setMerchantFilter(frontEndQuery, filter);
		setAttributeFilter(frontEndQuery, filter);
		setCityFilter(frontEndQuery, filter);
		setDistrictFilter(frontEndQuery, filter);
		setFeatureFilter(frontEndQuery, filter);
		collapse(solrParams, filter, frontEndQuery.getCityId());
		String[] filterArr = new String[filter.size()];
		filterArr = filter.toArray(filterArr);
		if (filterArr.length != 0) {
			solrParams.add(CommonParams.FQ, filterArr);
		}
	}

	private void sortQuery(ModifiableSolrParams params, String typeSort, String isAscending) {

		if (!Strings.isNullOrEmpty(typeSort)) {
			int isAsc = 0;
			String sort = SortQuery.DEFAULT;
			try {
				isAsc = Integer.parseInt(isAscending);
			} catch (Exception e) {
				getLogger().debug("invalid sort order..." + isAscending);
			}
			switch (typeSort.trim()) {
			case SortType.SORT_BY_BUY:
				sort = isAsc != 0 ? SortQuery.BUY_ASC : SortQuery.BUY_DESC;
				break;
			case SortType.SORT_BY_NEW:
				sort = isAsc != 0 ? SortQuery.NEW_ASC : SortQuery.NEW_DESC;
				break;
			case SortType.SORT_BY_VIEW:
				sort = isAsc != 0 ? SortQuery.VIEW_ASC : SortQuery.VIEW_DESC;
				break;
			case SortType.SORT_BY_PRICE:
				sort = isAsc != 0 ? SortQuery.PRICE_ASC : SortQuery.PRICE_DESC;
				break;
			case SortType.SORT_BY_QUANTITY:
				sort = isAsc != 0 ? SortQuery.QUANTITY_ASC : SortQuery.QUANTITY_DESC;
				break;
			case SortType.SORT_BY_VIEW_TOTAL:
				sort = isAsc != 0 ? SortQuery.VIEW_TOTAL_ASC : SortQuery.VIEW_TOTAL_DESC;
				break;
			case SortType.SORT_BY_VIEW_YEAR:
				sort = isAsc != 0 ? SortQuery.VIEW_YEAR_ASC : SortQuery.VIEW_YEAR_DESC;
				break;
			case SortType.SORT_BY_VIEW_MONTH:
				sort = isAsc != 0 ? SortQuery.VIEW_MONTH_ASC : SortQuery.VIEW_MONTH_DESC;
				break;
			case SortType.SORT_BY_VIEW_WEEK:
				sort = isAsc != 0 ? SortQuery.VIEW_WEEK_ASC : SortQuery.VIEW_WEEK_DESC;
				break;
			case SortType.SORT_BY_VIEW_DAY:
				sort = isAsc != 0 ? SortQuery.VIEW_DAY_ASC : SortQuery.VIEW_DAY_DESC;
				break;
			default:
				getLogger().debug("invalid sort type..." + typeSort);
			}
			params.set(CommonParams.SORT, sort);
		}
	}

	private void setBrandFilter(FrontEndQuery frontEndQuery, List<String> filter) {
		if (frontEndQuery.getIsGetBrand() == 1) {
			filter.add("brand_id:" + frontEndQuery.getBrandIds().get(0));
		} else if (frontEndQuery.getIsGetBrand() == 0) {
			if (frontEndQuery.getBrandIds() != null && !frontEndQuery.getBrandIds().isEmpty()) {
				Stream<String> brands = frontEndQuery.getBrandIds().stream().map(brandId -> "brand_id:" + brandId);
				filter.add(Joiner.on(" OR ").join(brands.iterator()));
			}
		}
	}

	private void setCategoryFilter(FrontEndQuery frontEndQuery, List<String> filter, ModifiableSolrParams solrParams) {
		if (!Strings.isNullOrEmpty(frontEndQuery.getCateogryId())
				|| !Strings.isNullOrEmpty(frontEndQuery.getFilteredCategoryId())) {
			int categoryId = -1;
			int filteredCategoryId = -1;
			try {
				categoryId = Integer.parseInt(frontEndQuery.getCateogryId());
			} catch (Exception e) {
			}
			try {
				filteredCategoryId = Integer.parseInt(frontEndQuery.getFilteredCategoryId());
			} catch (Exception e) {
			}
			if (filteredCategoryId > 0) {
				filter.add("category_id:" + frontEndQuery.getFilteredCategoryId());
			} else if (categoryId > 0) {
				filter.add("category_path:" + frontEndQuery.getCateogryId());
			}
		}
	}

	public void setPriceFilter(FrontEndQuery frontEndQuery, List<String> filter, ModifiableSolrParams solrParams) {
		if (frontEndQuery.getPrices() != null && !frontEndQuery.getPrices().isEmpty()) {
			solrParams.set("price", PRICE_LOCAL_PARAM);
			for (String s : frontEndQuery.getPrices()) {
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

	private void setAttributeFilter(FrontEndQuery frontEndQuery, List<String> filter) {
		Set<AttributeFilter> attributeFilters = frontEndQuery.getAttributeFilters();
		if (attributeFilters != null && !attributeFilters.isEmpty()) {
			attributeFilters.stream().forEach((attributeFilter) -> {
				filter.add(attributeFilter.getFilterQuery());
			});
		}
	}

	private void setFeatureFilter(FrontEndQuery frontEndQuery, List<String> filter) {
		String isPromotion = frontEndQuery.getIsPromotion();
		String isNew = frontEndQuery.getIsNew();
		long now = System.currentTimeMillis();
		if ((!Strings.isNullOrEmpty(isPromotion) && isPromotion.trim().equalsIgnoreCase("true"))
				&& (!Strings.isNullOrEmpty(isNew) && isNew.trim().equals("true"))) {
			// promotion, new
			filter.add(String.format(PROMOTION_QUERY, now, now, PRODUCT_ITEM_TYPE_PROMOTION, MERCHANT_PROMOTION_ID)
					+ " OR " + String.format(NEW_QUERY, now - SEVEN_DAYS));
		} else if (!Strings.isNullOrEmpty(isPromotion) && isPromotion.trim().equalsIgnoreCase("true")) {
			// promotion only
			filter.add(String.format(PROMOTION_QUERY, now, now, PRODUCT_ITEM_TYPE_PROMOTION, MERCHANT_PROMOTION_ID));
		} else if ((!Strings.isNullOrEmpty(isNew) && isNew.trim().equals("true"))) {
			// new only
			filter.add(String.format(NEW_QUERY, now - SEVEN_DAYS));
		}

	}

	private void setMerchantFilter(FrontEndQuery frontEndQuery, List<String> filter) {
		if (frontEndQuery.getMerchantIds() == null || frontEndQuery.getMerchantIds().isEmpty()) {
			return;
		}
		List<String> merchantIds = frontEndQuery.getMerchantIds();
		if (frontEndQuery.getIsGetMerchant() == 1) {
			filter.add("merchant_id:" + merchantIds.get(0));
		} else {
			List<String> filterMerchant = new LinkedList<>();
			for (String merchantId : frontEndQuery.getMerchantIds()) {
				if (!Strings.isNullOrEmpty(merchantId)) {
					filterMerchant.add("merchant_id:" + merchantId);
				}
			}
			filter.add(Joiner.on(" OR ").skipNulls().join(filterMerchant));
		}

	}

	private void setCityFilter(FrontEndQuery frontEndQuery, List<String> filter) {
		String cityId = frontEndQuery.getCityId();
		if (!Strings.isNullOrEmpty(cityId)) {
			filter.add("received_city_id:" + cityId);
			filter.add("served_province_ids:" + cityId + " OR (served_province_ids:0)");
			//			filter.add("city_" + cityId + "_score:[0 TO *]");
		}
	}

	private void setDistrictFilter(FrontEndQuery frontEndQuery, List<String> filter) {
		String districtId = frontEndQuery.getDistrict();
		if (!Strings.isNullOrEmpty(districtId))
			filter.add("served_district_ids:" + districtId + " OR served_district_ids:0 OR served_district_ids: "
					+ frontEndQuery.getCityId() + "_0");
	}

	private void useDismax(FrontEndQuery frontEndQuery, ModifiableSolrParams solrParams) {
		if (!frontEndQuery.getKeyword().equalsIgnoreCase("*:*")) {
			solrParams.add("defType", Constant.AUTO_PHRASE);
			solrParams.add("defType", Constant.EDISMAX);
			solrParams.set("qf", Constant.EDISMAX_QF);
			solrParams.set("pf", Constant.EDISMAX_PF);
			//			solrParams.set("mm", KeywordAccuracyGenerator.getInstance().GetAccuracyFromKey(frontEndQuery.getKeyword()));
		}
	}
}
