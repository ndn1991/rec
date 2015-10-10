/**
 * 
 */
package com.adr.bigdata.search.handler.eventdriven.impl;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.search.handler.db.sql.models.AbstractModel;
import com.adr.bigdata.search.handler.db.sql.models.CategoryTreeModel;
import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.EventHandler;
import com.adr.bigdata.search.handler.query.getfilter.builder.AbstractQueryBuilder;
import com.adr.bigdata.search.handler.query.getfilter.writer.AbstractResponseWriter;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
public class BaseEventHandler implements EventHandler, Loggable {
	protected static final String CACHE_NAME = "nativecache";
	protected AbstractQueryBuilder queryBuilder;
	protected AbstractResponseWriter writer;
	protected AbstractModel model;

	@Override
	public void onEvent(Event event) throws Exception {
		assert queryBuilder != null && writer != null && model != null;
	}

	/**
	 * writing the result
	 */
	protected void writeResponse(SolrQueryResponse response, Object... data) {
		writer.write(response, data);
	}

	/**
	 * building the query
	 */
	protected void buildQuery(SolrQueryRequest request, Object... data) {
		this.queryBuilder.build(request, data);
	}

	/**
	 * get the categoryTree from solr cache or from hazelcast
	 * 
	 * @param catId
	 * @param cache
	 * @return
	 */
	protected CategoryTreeVO getCatTree(int catId, SolrCache cache) {
		String key = "catTree_" + catId;
		Object catTree = cache.get(key);
		if (catTree == null) {
			try {
				catTree = ((CategoryTreeModel) model).getCategoryTree(catId);
				cache.put(key, catTree);
			} catch (Exception e) {
				getLogger().error("error getting catTree...{}", e.getMessage());
			}
		}
		return (CategoryTreeVO) catTree;
	}

}
