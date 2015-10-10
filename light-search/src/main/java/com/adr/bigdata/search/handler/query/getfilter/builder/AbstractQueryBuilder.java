/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.builder;

import java.util.Map;

import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.getfilter.factory.QueryFactory;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AbstractFilterStrategy;

/**
 * @author minhvv2
 *
 */
public abstract class AbstractQueryBuilder {
	protected static final String CACHE_NAME = "nativecache";

	protected AbstractQueryBuilder() {
		initDefaultFilters();
	}

	protected QueryFactory queryFactory;
	//minhvv2:each builder needs specified kind of filters, we store them here for later use
	protected Map<String, AbstractFilterStrategy> filtersContainer;

	//minhvv2: init the filter here
	protected abstract void initDefaultFilters();

	public abstract void build(SolrQueryRequest request, Object... data);
}
