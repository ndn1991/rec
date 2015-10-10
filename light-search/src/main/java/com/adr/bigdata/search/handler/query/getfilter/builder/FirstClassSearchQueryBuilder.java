/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.builder;

import java.util.HashMap;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.query.getfilter.bean.FirstClassSearchBean;
import com.adr.bigdata.search.handler.query.getfilter.factory.FirstClassSearchFactory;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AbstractFilterStrategy;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AttFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.BrandFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.CityFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FeaturedFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FilterType;
import com.adr.bigdata.search.handler.query.getfilter.strategy.MerchantFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.PriceFilter;

/**
 * @author minhvv2
 *
 */
public class FirstClassSearchQueryBuilder extends AbstractQueryBuilder {

	public FirstClassSearchQueryBuilder() {
		super();
		this.queryFactory = new FirstClassSearchFactory();
	}

	@Override
	protected void initDefaultFilters() {
		this.filtersContainer = new HashMap<String, AbstractFilterStrategy>();
		this.filtersContainer.put(FilterType.CITY_FILTER.toString(), new CityFilter());
		this.filtersContainer.put(FilterType.BRAND_FILTER.toString(), new BrandFilter());
		this.filtersContainer.put(FilterType.MERCHANT_FILTER.toString(), new MerchantFilter());
		this.filtersContainer.put(FilterType.PRICE_FILTER.toString(), new PriceFilter());
		this.filtersContainer.put(FilterType.FEATURED_FILTER.toString(), new FeaturedFilter());
		this.filtersContainer.put(FilterType.ATT_FILTER.toString(), new AttFilter());
	}

	@Override
	public void build(SolrQueryRequest request, Object... data) {
		FirstClassSearchBean query = (FirstClassSearchBean) this.queryFactory.create(request);
		assert data.length >= 1;
		FilterMessage mess = (FilterMessage) data[0];
		mess.getData().put("query", query);

		ModifiableSolrParams solrParams = new ModifiableSolrParams();

		FeaturedFilter featuredFilter = (FeaturedFilter) this.filtersContainer.get(FilterType.FEATURED_FILTER
				.toString());
		featuredFilter.enableFacet(solrParams);
		featuredFilter.enableCollapseBoostScore(solrParams, String.valueOf(query.getCityId()));
		featuredFilter.doFacet(solrParams);
		featuredFilter.doFilter(query.isNew(), query.isPromotion(), solrParams);

		BrandFilter brandFilter = (BrandFilter) this.filtersContainer.get(FilterType.BRAND_FILTER.toString());
		brandFilter.doFacet(solrParams);
		brandFilter.doFilter(query.getBrandIds(), solrParams);

		MerchantFilter merchantFilter = (MerchantFilter) this.filtersContainer.get(FilterType.MERCHANT_FILTER
				.toString());
		merchantFilter.doFacet(solrParams);
		merchantFilter.doFilter(query.getMerchantIds(), solrParams);

		CityFilter cityFilter = (CityFilter) this.filtersContainer.get(FilterType.CITY_FILTER.toString());
		cityFilter.doFilter(query.getCityId(), solrParams);

		AttFilter attFilter = (AttFilter) this.filtersContainer.get(FilterType.ATT_FILTER.toString());
		attFilter.doFacet(query.getCatId(), solrParams, mess, request.getSearcher().getCache(CACHE_NAME));
		attFilter.doFilter(query.getAttFilter(), solrParams);

		filterCategory(query.getCatId(), solrParams);
		filterKeyword(query.getKeyword(), solrParams);
		request.setParams(solrParams);
	}

	private void filterCategory(int catId, ModifiableSolrParams solrParams) {
		solrParams.add(CommonParams.FQ, "category_path:" + String.valueOf(catId));
	}

	private void filterKeyword(String keyword, ModifiableSolrParams solrParams) {
		solrParams.set(CommonParams.Q, keyword);
	}

}
