/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.builder;

import java.util.HashMap;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.query.getfilter.bean.MerchantQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.factory.MerchantQueryFactory;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AbstractFilterStrategy;
import com.adr.bigdata.search.handler.query.getfilter.strategy.BrandFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.CatFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FeaturedFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FilterType;
import com.adr.bigdata.search.handler.query.getfilter.strategy.PriceFilter;

/**
 * @author minhvv2
 *
 */
public class MerchantQueryBuilder extends AbstractQueryBuilder {
	public MerchantQueryBuilder() {
		super();
		this.queryFactory = new MerchantQueryFactory();
	}

	@Override
	protected void initDefaultFilters() {
		this.filtersContainer = new HashMap<String, AbstractFilterStrategy>();
		this.filtersContainer.put(FilterType.PRICE_FILTER.toString(), new PriceFilter());
		this.filtersContainer.put(FilterType.FEATURED_FILTER.toString(), new FeaturedFilter());
		this.filtersContainer.put(FilterType.BRAND_FILTER.toString(), new BrandFilter());
		this.filtersContainer.put(FilterType.CAT_FILTER.toString(), new CatFilter());
	}

	@Override
	public void build(SolrQueryRequest request, Object... data) {
		MerchantQueryBean query = (MerchantQueryBean) queryFactory.create(request);
		FilterMessage throwbackMess = (FilterMessage) data[0];
		throwbackMess.getData().put("query", query);
		ModifiableSolrParams solrParams = new ModifiableSolrParams();

		PriceFilter priceFilter = (PriceFilter) filtersContainer.get(FilterType.PRICE_FILTER.toString());
		priceFilter.enableFacet(solrParams);
		priceFilter.enableCollapseBoostScore(solrParams, "");
		priceFilter.doFacet(solrParams);
		priceFilter.doFilter(query.getPrice(), solrParams);

		FeaturedFilter featuredFilter = (FeaturedFilter) filtersContainer.get(FilterType.FEATURED_FILTER.toString());
		featuredFilter.doFacet(solrParams);
		featuredFilter.doFilter(query.isNew(), query.isPromotion(), solrParams);

		BrandFilter brandFilter = (BrandFilter) filtersContainer.get(FilterType.BRAND_FILTER.toString());
		brandFilter.doFacet(solrParams);
		if (query.getBrandIds() != null) {
			brandFilter.doFilter(query.getBrandIds(), solrParams);
		}

		CatFilter catFilter = (CatFilter) filtersContainer.get(FilterType.CAT_FILTER.toString());
		catFilter.doFacet(solrParams);
		if (query.getCatId() > 0) {
			catFilter.doFilter(query.getCatId(), false, solrParams);
		}

		request.setParams(solrParams);
	}

}
