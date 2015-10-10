/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.adr.bigdata.search.handler.query.cmbackend.CmBackendQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.ResponseStrategy;
import com.adr.bigdata.search.handler.responsestrategy.cmbackend.CmBackendResponseExecutor;
import com.nhb.common.Loggable;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

/**
 *
 * @author Tong Hoang Anh
 */
@SuppressWarnings("rawtypes")
public class CmBackendRequestHandler extends SearchHandler implements Loggable {

    private QueryBuilder builder;
    private ResponseStrategy responseExecutor;

    
	@Override
    public void init(NamedList args) {
        super.init(args);
        builder = new CmBackendQueryBuilder();
        responseExecutor = new CmBackendResponseExecutor();
    }

    @Override
    public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
        req.setParams(builder.buildSolrQuery(req));
        super.handleRequest(req, rsp);
        responseExecutor.execute(rsp, req);
    }
}
