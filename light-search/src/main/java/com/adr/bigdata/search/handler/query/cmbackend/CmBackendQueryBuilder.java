/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.query.cmbackend;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import static com.adr.bigdata.search.handler.query.cmbackend.CmBackendUtils.*;
import com.nhb.common.Loggable;

/**
 *
 * @author Tong Hoang Anh
 */
public class CmBackendQueryBuilder extends QueryBuilder implements Loggable{

    @Override
    public SolrParams buildSolrQuery(SolrQueryRequest request) {
        ModifiableSolrParams solrParam = new ModifiableSolrParams();
        CmBackendQuery cmBackendQuery = new CmBackendQuery(request);
        getLogger().debug("CM QUERY {}",cmBackendQuery);
        solrParam.set(CommonParams.Q, generateQuery(cmBackendQuery.getKeyword()));
        solrParam.set(CommonParams.START, cmBackendQuery.getStart());
        solrParam.set(CommonParams.ROWS, cmBackendQuery.getRows());
        setSort(solrParam, cmBackendQuery);   
        setFilter(solrParam, cmBackendQuery);
        return solrParam;
    }
    
}
