/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.strategy;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.google.common.base.Strings;

/**
 * @author minhvv2
 *
 */
public abstract class AbstractFilterStrategy {

	public void enableFacet(ModifiableSolrParams solrParams) {
		solrParams.set("facet", "true");
		solrParams.set("facet.mincount", "1");
		solrParams.set("facet.sort", "count");
		solrParams.set("facet.thread", "10");
		solrParams.set("rows", "0");
		solrParams.set("q", "*:*");
	}

	/**
	 * enable boost product via boost score since sprint 5
	 * 
	 * @param solrParams
	 */
	public void enableCollapseBoostScore(ModifiableSolrParams solrParams, String cityId) {
		String boostCityField = Strings.isNullOrEmpty(cityId) ? "city_0_score" : "city_" + cityId + "_score";
		String defaultScore = System.getProperty("solr.boostcity.minscore", "-10000000");
		String collapeCommand = "{!collapse field=product_item_group max="
				+ String.format("def(%s,%s)", boostCityField, defaultScore) + "}";
		solrParams.add(CommonParams.FQ, collapeCommand);
	}

	public abstract void doFilter(Object... data);

	public abstract void doFacet(Object... data);
}