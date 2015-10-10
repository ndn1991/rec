/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.search.handler.db.sql.models.CategoryTreeModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.entity.Message;
import com.adr.bigdata.search.handler.eventdriven.Callable;
import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.impl.BaseEventHandler;
import com.adr.bigdata.search.handler.eventdriven.impl.GetFilterEvent;
import com.adr.bigdata.search.handler.query.getfilter.builder.CatQueryBuilder;
import com.adr.bigdata.search.handler.query.getfilter.writer.CatQueryResponseWriter;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
public class CatQueryHandler extends BaseEventHandler implements Loggable {
	public CatQueryHandler() {
		this.queryBuilder = new CatQueryBuilder();
		this.writer = new CatQueryResponseWriter();
		this.model = ModelFactory.newInstance().getModel(CategoryTreeModel.class);
	}

	@Override
	public void onEvent(Event event) throws Exception {
		super.onEvent(event);
		GetFilterEvent filterEvent = (GetFilterEvent) event;
		SolrQueryRequest request = filterEvent.getSolrRequest();
		SolrQueryResponse response = filterEvent.getSolrResponse();
		Callable callback = filterEvent.getCallBack();
		Message throwbackMess = new FilterMessage();
		buildQuery(request, throwbackMess);

		SolrCache solrCache = request.getSearcher().getCache(CACHE_NAME);

		CategoryTreeVO catTree = getCatTree((int) throwbackMess.getData().get("catId"), solrCache);
		//throw back to solr handler
		callback.call(request, response);
		writeResponse(response, catTree, throwbackMess.getData().get("query"), solrCache,
				throwbackMess.getData().get("attMapping"));
	}

}