/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.search.handler.db.sql.models.MerchantModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.entity.Message;
import com.adr.bigdata.search.handler.eventdriven.Callable;
import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.impl.BaseEventHandler;
import com.adr.bigdata.search.handler.eventdriven.impl.GetFilterEvent;
import com.adr.bigdata.search.handler.query.getfilter.builder.MerchantQueryBuilder;
import com.adr.bigdata.search.handler.query.getfilter.writer.MerchantQueryResponseWriter;

/**
 * @author minhvv2
 *
 */
public class MerchantQueryHandler extends BaseEventHandler {

	public MerchantQueryHandler() {
		this.queryBuilder = new MerchantQueryBuilder();
		this.writer = new MerchantQueryResponseWriter();
		this.model = ModelFactory.newInstance().getModel(MerchantModel.class);
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
		SolrCache cache = (SolrCache) request.getSearcher().getCache(CACHE_NAME);
		//throw back to solr handler
		callback.call(request, response);
		writeResponse(response, cache, throwbackMess.getData().get("query"));
	}

}
