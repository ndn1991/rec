/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.strategy;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.adr.bigdata.search.handler.utils.SolrParamHelper;

/**
 * @author minhvv2
 *
 */
public class CatFilter extends AbstractFilterStrategy {

	@Override
	public void doFilter(Object... data) {
		assert data.length >= 2;
		int catId = (Integer) data[0];
		boolean isUsingPath = (Boolean) data[1];
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[2];
		if (isUsingPath) {
			solrParams.add(CommonParams.FQ, "{!tag=cat}category_path:" + String.valueOf(catId));
		} else {
			solrParams.add(CommonParams.FQ, "{!tag=cat}category_id:" + String.valueOf(catId));
		}
		SolrParamHelper.writeTaggedFacet(solrParams, "category_id_facet", "{!ex=cat}category_id_facet");
	}

	@Override
	public void doFacet(Object... data) {
		assert data.length >= 1;
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[0];
		solrParams.add("facet.field", "category_id_facet");
		solrParams.set("f.category_id_facet.facet.limit", "10");
	}

}
