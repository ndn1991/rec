/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.builder;

import java.util.HashMap;

import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.eventdriven.impl.BaseEventDispatcher;
import com.adr.bigdata.search.handler.eventdriven.impl.CatBuildingEvent;
import com.adr.bigdata.search.handler.query.getfilter.bean.CatQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.factory.CatQueryFactory;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AbstractFilterStrategy;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FeaturedFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.BrandFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.MerchantFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.CityFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FilterType;

/**
 * @author minhvv2
 *
 */
public class CatQueryBuilder extends AbstractQueryBuilder {
	private final CatBuildingDispatcher dispatcher;

	public CatQueryBuilder() {
		super();
		this.queryFactory = new CatQueryFactory();
		dispatcher = new CatBuildingDispatcher();
		dispatcher.addEventListener(CatType.FIRST_CLASS.toString(), new FirstClassCatBuildingHandler());
		dispatcher.addEventListener(CatType.NORMAL_CAT.toString(), new NormalCatBuildingHandler());
	}

	@Override
	protected void initDefaultFilters() {
		filtersContainer = new HashMap<String, AbstractFilterStrategy>();
		filtersContainer.put(FilterType.FEATURED_FILTER.toString(), new FeaturedFilter());
		filtersContainer.put(FilterType.BRAND_FILTER.toString(), new BrandFilter());
		filtersContainer.put(FilterType.MERCHANT_FILTER.toString(), new MerchantFilter());
		filtersContainer.put(FilterType.CITY_FILTER.toString(), new CityFilter());
	}

	@Override
	public void build(SolrQueryRequest request, Object... data) {
		CatQueryBean query = (CatQueryBean) queryFactory.create(request);
		assert data.length >= 1;
		FilterMessage mess = (FilterMessage) data[0];
		mess.getData().put("catId", query.getCatId());
		mess.getData().put("query", query);
		dispatcher.dispatchEvent(String.valueOf(query.getCatTypeId()), request, query, filtersContainer, mess);
	}

	private static class CatBuildingDispatcher extends BaseEventDispatcher {
		@Override
		public void dispatchEvent(String eventType, Object... data) {
			this.dispatchEvent(new CatBuildingEvent(eventType, data));
		}

	}

}
