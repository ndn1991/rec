/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.db.sql.models.CategoryModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.eventdriven.Callable;
import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.impl.BaseEventHandler;
import com.adr.bigdata.search.handler.eventdriven.impl.GetFilterEvent;
import com.adr.bigdata.search.handler.query.getfilter.builder.NormalSearchQueryBuilder;
import com.adr.bigdata.search.handler.query.getfilter.writer.NormalSearchResponseWriter;

/**
 * @author minhvv2
 *
 */
public class NormalSearchHandler extends BaseEventHandler {
	public NormalSearchHandler() {
		this.queryBuilder = new NormalSearchQueryBuilder();
		this.writer = new NormalSearchResponseWriter();
		this.model = ModelFactory.newInstance().getModel(CategoryModel.class);
	}

	@Override
	public void onEvent(Event event) throws Exception {
		super.onEvent(event);
		GetFilterEvent filterEvent = (GetFilterEvent) event;
		SolrQueryRequest request = filterEvent.getSolrRequest();
		SolrQueryResponse response = filterEvent.getSolrResponse();
		Callable callback = filterEvent.getCallBack();
		FilterMessage throwbackMess = new FilterMessage();
		this.buildQuery(request, throwbackMess);
		//throw back to solr handler
		callback.call(request, response);
		this.writeResponse(response, request.getSearcher().getCache(CACHE_NAME), throwbackMess);
	}

}