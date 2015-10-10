/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.builder;

import java.util.Map;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.EventHandler;
import com.adr.bigdata.search.handler.eventdriven.impl.CatBuildingEvent;
import com.adr.bigdata.search.handler.query.getfilter.bean.CatQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AbstractFilterStrategy;
import com.adr.bigdata.search.handler.query.getfilter.strategy.BrandFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.CityFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FeaturedFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FilterType;
import com.adr.bigdata.search.handler.query.getfilter.strategy.MerchantFilter;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
class FirstClassCatBuildingHandler implements EventHandler, Loggable {
	protected static final long IS_NEW_TIME_WINDOWS = 604800000;// 7 days

	@Override
	public void onEvent(Event event) throws Exception {
		CatBuildingEvent catEvent = (CatBuildingEvent) event;
		SolrQueryRequest request = catEvent.getSolrRequest();
		CatQueryBean query = catEvent.getQueryBean();
		Map<String, AbstractFilterStrategy> name2Filters = catEvent.getName2Filter();
		FeaturedFilter featuredFilter = (FeaturedFilter) name2Filters.get(FilterType.FEATURED_FILTER.toString());
		BrandFilter brandFilter = (BrandFilter) name2Filters.get(FilterType.BRAND_FILTER.toString());
		MerchantFilter merchantFilter = (MerchantFilter) name2Filters.get(FilterType.MERCHANT_FILTER.toString());
		CityFilter cityFilter = (CityFilter) name2Filters.get(FilterType.CITY_FILTER.toString());
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		featuredFilter.enableFacet(solrParams);
		featuredFilter.enableCollapseBoostScore(solrParams, String.valueOf(query.getCatId()));
		featuredFilter.doFacet(solrParams);
		featuredFilter.doFilter(query.isNew(), query.isPromotion(), solrParams);
		brandFilter.doFacet(solrParams);
		brandFilter.doFilter(query.getBrandIds(), solrParams);
		merchantFilter.doFacet(solrParams);
		merchantFilter.doFilter(query.getMerchantIds(), solrParams);
		cityFilter.doFilter(query.getCityId(), solrParams);
		filterCategory(query.getCatId(), solrParams);
		request.setParams(solrParams);
	}

	private void filterCategory(int catId, ModifiableSolrParams solrParams) {
		solrParams.add(CommonParams.FQ, "category_path:" + String.valueOf(catId));
	}

}