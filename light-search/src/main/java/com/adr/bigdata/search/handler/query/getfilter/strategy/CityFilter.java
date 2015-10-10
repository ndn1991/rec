/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.strategy;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

/**
 * @author minhvv2
 *
 */
public class CityFilter extends AbstractFilterStrategy {

	@Override
	public void doFilter(Object... data) {
		assert data.length >= 2;
		int cityId = (Integer) data[0];
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[1];
		solrParams.add(CommonParams.FQ, "received_city_id:" + cityId);
	}

	@Override
	public void doFacet(Object... data) {
		// minhvv2: no facet city yet
		// waiting for business logic
	}

}
