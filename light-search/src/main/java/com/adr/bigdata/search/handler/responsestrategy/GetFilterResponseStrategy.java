package com.adr.bigdata.search.handler.responsestrategy;

import java.util.List;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.query.frontend.GetFilterQuery;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;

public interface GetFilterResponseStrategy extends ResponseStrategy {
	public void makeUpResponseFromRequest(SolrQueryResponse rsp, SolrQueryRequest req, GetFilterQuery filterQuery);

	public void writeResponseWithCacheCategoryTree(SolrQueryResponse rsp, SolrQueryRequest req,
			GetFilterQuery filterQuery, CategoryTreeVO cachedTree);

	public List<NamedList> genListFeatured(SolrQueryResponse rsp);
}
