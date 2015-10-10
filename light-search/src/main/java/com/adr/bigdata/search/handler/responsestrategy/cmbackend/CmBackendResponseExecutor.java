/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.responsestrategy.cmbackend;

import com.adr.bigdata.search.handler.responsestrategy.ResponseStrategy;
import com.nhb.common.Loggable;

import java.util.Collections;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

/**
 *
 * @author Tong Hoang Anh
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CmBackendResponseExecutor implements ResponseStrategy, Loggable {

	@Override
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		Object grouped = rsp.getValues().get("grouped");
		if (grouped == null) {
			rsp.getValues().addAll(ZERO_RESULT);
		}else{
			rsp.getValues().remove("grouped");
			if (grouped instanceof SimpleOrderedMap) {
				SimpleOrderedMap spMap = (SimpleOrderedMap) grouped;
				Object productItemGroup = spMap
						.get(req.getParams().get("group.field", "product_item_id_merchant_id"));
				rsp.getValues().addAll((NamedList) productItemGroup);
			} else {
				getLogger().error("invalid return format");
				rsp.getValues().addAll(ZERO_RESULT);
			}
		}
	}

	private final static NamedList ZERO_RESULT;

	static {
		ZERO_RESULT = new SimpleOrderedMap();
		ZERO_RESULT.add("matches", 0);
		ZERO_RESULT.add("ngroups", 0);
		ZERO_RESULT.add("group", Collections.EMPTY_LIST);
	}
}
