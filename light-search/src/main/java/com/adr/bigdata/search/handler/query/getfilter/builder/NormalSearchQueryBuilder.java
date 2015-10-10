/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.builder;

import java.util.HashMap;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.query.getfilter.bean.NormalSearchBean;
import com.adr.bigdata.search.handler.query.getfilter.factory.NormalSearchFactory;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AbstractFilterStrategy;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AttFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.BrandFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.CatFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.CityFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FeaturedFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FilterType;
import com.adr.bigdata.search.handler.query.getfilter.strategy.MerchantFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.PriceFilter;

/**
 * @author minhvv2
 *
 */
public class NormalSearchQueryBuilder extends AbstractQueryBuilder {

	public NormalSearchQueryBuilder() {
		super();
		this.queryFactory = new NormalSearchFactory();
	}

	@Override
	protected void initDefaultFilters() {
		this.filtersContainer = new HashMap<String, AbstractFilterStrategy>();
		this.filtersContainer.put(FilterType.CITY_FILTER.toString(), new CityFilter());
		this.filtersContainer.put(FilterType.CAT_FILTER.toString(), new CatFilter());
		this.filtersContainer.put(FilterType.BRAND_FILTER.toString(), new BrandFilter());
		this.filtersContainer.put(FilterType.MERCHANT_FILTER.toString(), new MerchantFilter());
		this.filtersContainer.put(FilterType.PRICE_FILTER.toString(), new PriceFilter());
		this.filtersContainer.put(FilterType.FEATURED_FILTER.toString(), new FeaturedFilter());
		this.filtersContainer.put(FilterType.ATT_FILTER.toString(), new AttFilter());
	}

	@Override
	public void build(SolrQueryRequest request, Object... data) {
		NormalSearchBean query = (NormalSearchBean) this.queryFactory.create(request);
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		FilterMessage mess = (FilterMessage) data[0];
		mess.getData().put("query", query);

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

		CatFilter catFilter = (CatFilter) this.filtersContainer.get(FilterType.CAT_FILTER.toString());
		catFilter.doFacet(solrParams);
		catFilter.doFilter(query.getCatId(), true, solrParams);

		if (query.getCatId() > 0) {
			AttFilter attFilter = (AttFilter) this.filtersContainer.get(FilterType.ATT_FILTER.toString());
			attFilter.doFacet(query.getCatId(), solrParams, mess, request.getSearcher().getCache(CACHE_NAME));
			attFilter.doFilter(query.getAttFilter(), solrParams);
		}

		CityFilter cityFilter = (CityFilter) this.filtersContainer.get(FilterType.CITY_FILTER.toString());
		cityFilter.doFilter(query.getCityId(), solrParams);

		PriceFilter priceFilter = (PriceFilter) this.filtersContainer.get(FilterType.PRICE_FILTER.toString());
		priceFilter.doFacet(solrParams);
		priceFilter.doFilter(query.getPrice(), solrParams);

		filterKeyword(query.getKeyword(), solrParams);
		request.setParams(solrParams);
	}

	private void filterKeyword(String keyword, ModifiableSolrParams solrParams) {
		solrParams.set(CommonParams.Q, keyword);
	}

}
