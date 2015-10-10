/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.builder;

import java.util.Map;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.EventHandler;
import com.adr.bigdata.search.handler.eventdriven.impl.CatBuildingEvent;
import com.adr.bigdata.search.handler.query.getfilter.bean.CatQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AbstractFilterStrategy;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AttFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.BrandFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FeaturedFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FilterType;
import com.adr.bigdata.search.handler.query.getfilter.strategy.MerchantFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.PriceFilter;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
class NormalCatBuildingHandler implements EventHandler, Loggable {
	private static final String CACHE_NAME = "nativecache";

	@Override
	public void onEvent(Event event) throws Exception {
		CatBuildingEvent catEvent = (CatBuildingEvent) event;
		SolrQueryRequest request = catEvent.getSolrRequest();
		CatQueryBean query = catEvent.getQueryBean();
		Map<String, AbstractFilterStrategy> name2Filters = catEvent.getName2Filter();
		FilterMessage mess = catEvent.getMess();
		ModifiableSolrParams solrParams = new ModifiableSolrParams();

		FeaturedFilter featuredFilter = (FeaturedFilter) name2Filters.get(FilterType.FEATURED_FILTER.toString());
		featuredFilter.enableFacet(solrParams);
		featuredFilter.enableCollapseBoostScore(solrParams, String.valueOf(query.getCityId()));
		featuredFilter.doFacet(solrParams);
		featuredFilter.doFilter(query.isNew(), query.isPromotion(), solrParams);

		BrandFilter brandFilter = (BrandFilter) name2Filters.get(FilterType.BRAND_FILTER.toString());
		brandFilter.doFacet(solrParams);
		brandFilter.doFilter(query.getBrandIds(), solrParams);

		MerchantFilter merchantFilter = (MerchantFilter) name2Filters.get(FilterType.MERCHANT_FILTER.toString());
		merchantFilter.doFacet(solrParams);
		merchantFilter.doFilter(query.getMerchantIds(), solrParams);

		PriceFilter priceFilter = (PriceFilter) name2Filters.get(FilterType.PRICE_FILTER.toString());
		priceFilter.doFacet(solrParams);
		priceFilter.doFilter(query.getPrices(), solrParams);

		AttFilter attFilter = (AttFilter) name2Filters.get(FilterType.ATT_FILTER.toString());
		attFilter.doFacet(query.getCatId(), solrParams, mess, request.getSearcher().getCache(CACHE_NAME));
		attFilter.doFilter(query.getAttFilter(), solrParams);

		filterCategory(query.getCatId(), solrParams);

		request.setParams(solrParams);
	}

	private void filterCategory(int catId, ModifiableSolrParams solrParams) {
		solrParams.add(CommonParams.FQ, "category_path:" + String.valueOf(catId));
	}

}