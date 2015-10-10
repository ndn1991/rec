package com.adr.bigdata.search.handler.utils;

import org.apache.solr.common.params.ModifiableSolrParams;

import com.adr.bigdata.search.handler.query.frontend.GetFilterQuery;

public class EdisMaxUtils {
	public static void useDismax(GetFilterQuery getFilterQuery, ModifiableSolrParams solrParams) {
		if (!getFilterQuery.getKeyword().equalsIgnoreCase("*:*") && !getFilterQuery.isSearchBySku()) {
			solrParams.add("defType", Constant.AUTO_PHRASE);
			solrParams.add("defType", Constant.EDISMAX);
			solrParams.set("qf", Constant.EDISMAX_QF);
			solrParams.set("pf", Constant.EDISMAX_PF);
			//			solrParams
			//					.set("mm", KeywordAccuracyGenerator.getInstance().GetAccuracyFromKey(getFilterQuery.getKeyword()));
		}
	}
}
