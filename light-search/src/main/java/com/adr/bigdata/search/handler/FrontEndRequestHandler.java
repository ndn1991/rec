/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.query.frontend.FrontEndQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.FrontendResponseExecutor;

/**
 *
 * @author Tong Hoang Anh
 */
@SuppressWarnings("rawtypes")
public class FrontEndRequestHandler extends AdrSearchBaseHandler {

	@Override
	public void init(NamedList args) {
		super.init(args);		
		responseExecutor = new FrontendResponseExecutor();
		builder = new FrontEndQueryBuilder();
	}

	@Override
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
		super.handleRequest(req, rsp);
	}

}
