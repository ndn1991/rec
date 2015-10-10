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
import com.adr.bigdata.search.handler.query.getfilter.bean.FirstClassSearchBean;
import com.adr.bigdata.search.handler.query.getfilter.builder.FirstClassSearchQueryBuilder;
import com.adr.bigdata.search.handler.query.getfilter.writer.FirstClassSearchResponseWriter;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;

/**
 * @author minhvv2
 *
 */
public class FirstClassSearchHandler extends BaseEventHandler {
	public FirstClassSearchHandler() {
		this.queryBuilder = new FirstClassSearchQueryBuilder();
		this.writer = new FirstClassSearchResponseWriter();
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
		this.buildQuery(request, throwbackMess);
		SolrCache solrCache = request.getSearcher().getCache(CACHE_NAME);
		FirstClassSearchBean query = (FirstClassSearchBean) throwbackMess.getData().get("query");
		CategoryTreeVO catTree = getCatTree(query.getCatId(), solrCache);
		//throw back to solr handler
		callback.call(request, response);
		this.writeResponse(response, solrCache, catTree, query, throwbackMess.getData().get("attMapping"));
	}

}
