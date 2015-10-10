/**
 * 
 */
package com.adr.bigdata.search.handler.eventdriven.impl;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.eventdriven.Callable;
import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.EventDispatcher;

/**
 * @author minhvv2
 *
 */
public class GetFilterEvent implements Event {
	private Callable callback;
	private String type;
	private EventDispatcher target;
	private SolrQueryRequest solrRequest;
	private SolrQueryResponse solrResponse;

	/**
	 * @param eventType
	 * @param data
	 */
	public GetFilterEvent(String eventType, Object... data) {
		this.type = eventType;
		assert data.length == 3;
		solrRequest = (SolrQueryRequest) data[0];
		solrResponse = (SolrQueryResponse) data[1];
		callback = (Callable) data[2];
	}

	public SolrQueryRequest getSolrRequest() {
		return solrRequest;
	}

	public void setSolrRequest(SolrQueryRequest solrRequest) {
		this.solrRequest = solrRequest;
	}

	public SolrQueryResponse getSolrResponse() {
		return solrResponse;
	}

	public void setSolrResponse(SolrQueryResponse solrResponse) {
		this.solrResponse = solrResponse;
	}

	@Override
	public void setType(String type) {
		this.type = type;

	}

	@Override
	public String getType() {
		return this.type;
	}

	@Override
	public <T extends EventDispatcher> T getTarget() {
		return (T) target;
	}

	@Override
	public void setTarget(EventDispatcher target) {
		this.target = target;
	}

	@Override
	public Callable getCallBack() {
		return callback;
	}

	@Override
	public void setCallBack(Callable callback) {
		this.callback = callback;
	}

}
