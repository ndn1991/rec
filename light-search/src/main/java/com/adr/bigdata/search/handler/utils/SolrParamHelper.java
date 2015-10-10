package com.adr.bigdata.search.handler.utils;

import org.apache.solr.common.params.ModifiableSolrParams;

public class SolrParamHelper {
	public static void writeTaggedFacet(ModifiableSolrParams solrParam, String oldFacetVal, String excludedFacetVal) {
		solrParam.remove("facet.field", oldFacetVal);
		solrParam.add("facet.field", excludedFacetVal);
	}

}
