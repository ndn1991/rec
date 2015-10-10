package com.adr.bigdata.search.handler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.search.handler.eventdriven.Callable;
import com.adr.bigdata.search.handler.eventdriven.impl.BaseEventDispatcher;
import com.adr.bigdata.search.handler.query.getfilter.BrandQueryHandler;
import com.adr.bigdata.search.handler.query.getfilter.CatQueryHandler;
import com.adr.bigdata.search.handler.query.getfilter.FirstClassSearchHandler;
import com.adr.bigdata.search.handler.query.getfilter.MerchantQueryHandler;
import com.adr.bigdata.search.handler.query.getfilter.NormalSearchHandler;
import com.adr.bigdata.search.handler.query.getfilter.QueryType;
import com.nhb.common.utils.Initializer;

/**
 * @author minhvv2
 *
 */
public class NewGetFilterRequestHandler extends SearchHandler {
	private BaseEventDispatcher dispatcher;
	private boolean isInitInvalidateCache;

	@Override
	public void init(NamedList params) {
		super.init(params);
		isInitInvalidateCache = false;
		//init system properties
		if (System.getProperty("db.name", "notexist").equals("notexist")) {
			Initializer.bootstrap(this.getClass());
		}
		HazelcastClientAdapter.getInstance();//init hazelcast for application
		dispatcher = new BaseEventDispatcher();
		dispatcher.addEventListener(QueryType.BROWSE_BY_CAT.toString(), new CatQueryHandler());
		dispatcher.addEventListener(QueryType.BROWSE_BY_MERCHANT.toString(), new MerchantQueryHandler());
		dispatcher.addEventListener(QueryType.BROWSE_BY_BRAND.toString(), new BrandQueryHandler());
		dispatcher.addEventListener(QueryType.FIRST_CLASS_SEARCH.toString(), new FirstClassSearchHandler());
		dispatcher.addEventListener(QueryType.NORMAL_SEARCH.toString(), new NormalSearchHandler());
	}

	@Override
	public void handleRequest(SolrQueryRequest request, SolrQueryResponse response) {
		//TODO: add collapse boost score and city boostscore, district filter

		if (!isInitInvalidateCache) {
			cacheInvalidate(request.getSearcher().getCache("nativecache"));
		}
		this.dispatchEvent(request.getParams().get("querytype"), request, response, new Callable() {
			@Override
			public void call(Object... data) {
				assert data.length == 2;//make sure data is completely passed back
				SolrQueryRequest request = (SolrQueryRequest) data[0];
				SolrQueryResponse response = (SolrQueryResponse) data[1];
				NewGetFilterRequestHandler.super.handleRequest(request, response);
			}
		});
	}

	private void dispatchEvent(String eventType, Object... data) {
		dispatcher.dispatchEvent(eventType, data);
	}

	private void cacheInvalidate(SolrCache cache) {
		isInitInvalidateCache = true;
		final int delay = Integer.valueOf(System.getProperty("solr.cache.invalidate.time", "600")); // delay for 10 mins
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				cache.clear();
				// don't need to synchronized because other threads are allowed to read dirty state
			}
		}, 1, delay, TimeUnit.SECONDS);

	}
}
