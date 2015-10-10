package com.adr.bigdata.search.handler.utils;

import org.apache.solr.response.SolrQueryResponse;

public class SolrQueryResponseHelper {
	/**
	 * erase the response with specified key return the erased object
	 * 
	 * @param response
	 * @param key
	 * @return
	 */
	public static Object erase(SolrQueryResponse response, String key) {
		Object result = response.getValues().get(key);
		if (result != null) {
			response.getValues().remove(key);
		}
		return result;
	}

	/**
	 * write a customized field to response
	 * @param response
	 * @param key
	 * @param val
	 */
	public static void write2Response(SolrQueryResponse response, String key, Object val) {
		response.getValues().add(key, val);
	}
}
