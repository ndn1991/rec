/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.db.sql.models.AbstractModel;

/**
 * @author minhvv2
 *
 */
public abstract class WriterStrategy {
	protected AbstractModel model;

	public abstract void writeComponent(SolrQueryResponse response, NamedList value, Object... data);
}
