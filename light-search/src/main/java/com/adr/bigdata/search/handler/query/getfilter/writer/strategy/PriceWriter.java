/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.response.daos.SolrResponseHelper;
import com.adr.bigdata.search.handler.utils.CommonConstant;

/**
 * @author minhvv2
 *
 */
public class PriceWriter extends WriterStrategy {

	@Override
	public void writeComponent(SolrQueryResponse response, NamedList stats, Object... data) {
		if (stats != null) {
			NamedList statField = (NamedList) stats.get(CommonConstant.STATS_FIELDS);
			if (statField != null) {
				response.add(CommonConstant.MAX_PRICE, SolrResponseHelper.extractPriceFromStatsNamedList(stats));
			}
		}
	}

}
