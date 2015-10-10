package com.adr.bigdata.search.handler.getfilterconsumer;

import org.apache.solr.search.SolrCache;

public class CacheEvent {
	private SolrCache cache;

	public SolrCache getCache() {
		return cache;
	}

	public void setCache(SolrCache cache) {
		this.cache = cache;
	}
}
