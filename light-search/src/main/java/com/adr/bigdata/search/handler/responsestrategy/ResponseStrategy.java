/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.responsestrategy;

import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.nhb.common.Loggable;

/**
 *
 * @author Tong Hoang Anh
 */
public interface ResponseStrategy extends Loggable {

	/*
	 * make the response
	 */
	void execute(SolrQueryResponse rsp, SolrQueryRequest req);

}
