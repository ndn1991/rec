/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

import org.apache.solr.response.SolrQueryResponse;

/**
 * @author minhvv2
 *
 */
public interface DescriptableWriter {
	public void writeInfor(SolrQueryResponse response, int id, Object... data);
}
