/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.db.sql.models.BrandModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.entity.Message;
import com.adr.bigdata.search.handler.eventdriven.Callable;
import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.impl.BaseEventHandler;
import com.adr.bigdata.search.handler.eventdriven.impl.GetFilterEvent;
import com.adr.bigdata.search.handler.query.getfilter.builder.BrandQueryBuilder;
import com.adr.bigdata.search.handler.query.getfilter.writer.BrandQueryResponseWriter;

/**
 * @author minhvv2
 *
 */
public class BrandQueryHandler extends BaseEventHandler {

	public BrandQueryHandler() {
		this.queryBuilder = new BrandQueryBuilder();
		this.writer = new BrandQueryResponseWriter();
		this.model = ModelFactory.newInstance().getModel(BrandModel.class);
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
		//throw back to solr handler
		callback.call(request, response);
		writeResponse(response, request.getSearcher().getCache(CACHE_NAME), throwbackMess.getData().get("brandId"));
	}

}
