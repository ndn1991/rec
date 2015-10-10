package com.adr.bigdata.search.handler.responsestrategy;

import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.db.sql.models.FilterModel;
import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.facetextractor.FacetResultExtractor;
import com.adr.bigdata.search.handler.query.frontend.GetFilterQuery;
import com.adr.bigdata.search.handler.response.daos.FilterResponseDataAccess;
import com.adr.bigdata.search.handler.response.daos.SolrResponseHelper;
import com.adr.bigdata.search.handler.utils.CommonConstant;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;

public class GetFilterResponseExecutor implements GetFilterResponseStrategy {

	private final FilterResponseDataAccess filterDAO;

	public GetFilterResponseExecutor(FilterModel filterModel, ExecutorService pool) {
		this.filterDAO = new FilterResponseDataAccess(filterModel, pool);
	}

	@Override
	public void makeUpResponseFromRequest(SolrQueryResponse rsp, SolrQueryRequest req, GetFilterQuery filterQuery) {

		SolrQueryResponseHelper.erase(rsp, "grouped");
		SolrQueryResponseHelper.erase(rsp, "response");

		NamedList facetCounts = (NamedList) rsp.getValues().get(FacetConstant.FACET_COUNTS);
		rsp.getValues().remove(FacetConstant.FACET_COUNTS);

		NamedList stats = (NamedList) rsp.getValues().get(FacetConstant.STATS);
		rsp.getValues().remove(FacetConstant.STATS);
		if (stats != null) {
			NamedList statField = (NamedList) stats.get(CommonConstant.STATS_FIELDS);
			if (statField != null) {
				rsp.add(CommonConstant.MAX_PRICE, SolrResponseHelper.extractPriceFromStatsNamedList(stats));
			}
		}

		try {
			filterDAO.writeToResponse(filterQuery, rsp, req, facetCounts);
		} catch (Exception e) {
			getLogger().error("error modifying result...{}", e.getMessage());
		}
	}

	@Override
	public void writeResponseWithCacheCategoryTree(SolrQueryResponse rsp, SolrQueryRequest req,
			GetFilterQuery filterQuery, CategoryTreeVO cachedTree) {
		SolrQueryResponseHelper.erase(rsp, "grouped");
		SolrQueryResponseHelper.erase(rsp, "response");

		NamedList facetCounts = (NamedList) rsp.getValues().get(FacetConstant.FACET_COUNTS);
		rsp.getValues().remove(FacetConstant.FACET_COUNTS);

		NamedList stats = (NamedList) rsp.getValues().get(FacetConstant.STATS);
		rsp.getValues().remove(FacetConstant.STATS);

		if (stats != null) {
			rsp.add(CommonConstant.MAX_PRICE, SolrResponseHelper.extractPriceFromStatsNamedList(stats));
		}
		try {
			filterDAO.writeCateBrowsingQuery(filterQuery, rsp, req, facetCounts, cachedTree);
		} catch (Exception e) {
			getLogger().error("error writing result to response...{}", e);
		}
	}

	@Override
	public List<NamedList> genListFeatured(SolrQueryResponse rsp) {
		NamedList facetCounts = (NamedList) rsp.getValues().get(FacetConstant.FACET_COUNTS);
		FacetResultExtractor extractor = new FacetResultExtractor(facetCounts);
		return extractor.createListFeatured();
	}

	@Override
	@Deprecated
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		SimpleOrderedMap spMap = (SimpleOrderedMap) rsp.getValues().get(CommonConstant.GROUPED);
		rsp.getValues().remove(CommonConstant.GROUPED);

		NamedList facetCounts = (NamedList) rsp.getValues().get(FacetConstant.FACET_COUNTS);
		rsp.getValues().remove(FacetConstant.FACET_COUNTS);

		if (spMap != null) {
			SimpleOrderedMap spMapResult = (SimpleOrderedMap) spMap.getVal(0);
			rsp.add(CommonConstant.NUMFOUND, spMapResult.getVal(0));
		} else {
			rsp.add(CommonConstant.NUMFOUND, 0);
		}
		try {
			GetFilterQuery filterQuery = new GetFilterQuery(req);
			filterDAO.writeToResponse(filterQuery, rsp, req, facetCounts);
		} catch (Exception e) {
			getLogger().error("error modifying result..." + e.getMessage());
			e.printStackTrace();
		}
	}

}
