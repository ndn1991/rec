/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.strategy;

import java.util.List;
import java.util.stream.Stream;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.adr.bigdata.search.handler.utils.SolrParamHelper;
import com.google.common.base.Joiner;

/**
 * @author minhvv2
 *
 */
public class BrandFilter extends AbstractFilterStrategy {

	@Override
	public void doFilter(Object... data) {
		assert data.length >= 2;
		List<Integer> brandIds = (List<Integer>) data[0];
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[1];
		if (brandIds != null) {
			Stream<String> brands = brandIds.stream().map(brandId -> "brand_id:" + brandId.toString());
			solrParams.add(CommonParams.FQ, "{!tag=brand}" + Joiner.on(" OR ").join(brands.iterator()));
			SolrParamHelper.writeTaggedFacet(solrParams, "brand_id_facet", "{!ex=brand}brand_id_facet");
		}
	}

	@Override
	public void doFacet(Object... data) {
		assert data.length >= 1;
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[0];
		solrParams.add("facet.field", "brand_id_facet");
	}
}
