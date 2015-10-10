/**
 * 
 */
package com.adr.bigdata.search.handler.eventdriven.impl;

import java.util.Map;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.eventdriven.Callable;
import com.adr.bigdata.search.handler.eventdriven.Event;
import com.adr.bigdata.search.handler.eventdriven.EventDispatcher;
import com.adr.bigdata.search.handler.query.getfilter.bean.CatQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.builder.CatType;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AbstractFilterStrategy;
import com.adr.bigdata.search.handler.query.getfilter.strategy.AttFilter;
import com.adr.bigdata.search.handler.query.getfilter.strategy.FilterType;
import com.adr.bigdata.search.handler.query.getfilter.strategy.PriceFilter;

/**
 * @author minhvv2
 *
 */
public class CatBuildingEvent implements Event {
	private CatQueryBean queryBean;
	private Callable callback;
	private String type;
	private EventDispatcher target;
	private SolrQueryRequest solrRequest;
	private SolrQueryResponse solrResponse;
	private FilterMessage mess;
	private Map<String, AbstractFilterStrategy> name2Filter;

	/**
	 * @param eventType
	 * @param data
	 */
	public CatBuildingEvent(String eventType, Object... data) {
		this.type = eventType;
		assert data.length >= 2;
		solrRequest = (SolrQueryRequest) data[0];
		queryBean = (CatQueryBean) data[1];
		name2Filter = (Map<String, AbstractFilterStrategy>) data[2];
		this.mess = (FilterMessage) data[3];
		if (eventType.equals(CatType.NORMAL_CAT.toString())) {
			name2Filter.put(FilterType.PRICE_FILTER.toString(), new PriceFilter());
			name2Filter.put(FilterType.ATT_FILTER.toString(), new AttFilter());
		}
	}

	public FilterMessage getMess() {
		return mess;
	}

	public void setMess(FilterMessage mess) {
		this.mess = mess;
	}

	public Map<String, AbstractFilterStrategy> getName2Filter() {
		return name2Filter;
	}

	public void setName2Filter(Map<String, AbstractFilterStrategy> name2Filter) {
		this.name2Filter = name2Filter;
	}

	public CatQueryBean getQueryBean() {
		return queryBean;
	}

	public void setQueryBean(CatQueryBean queryBean) {
		this.queryBean = queryBean;
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
