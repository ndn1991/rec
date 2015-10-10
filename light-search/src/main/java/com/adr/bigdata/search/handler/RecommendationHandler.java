/**
 * 
 */
package com.adr.bigdata.search.handler;

import java.util.List;

import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrQueryRequestBase;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.query.frontend.RecommendationQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.RecommendationResponseExecutor;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
public class RecommendationHandler extends SearchHandler implements Loggable {
	private static final String PRODUCT_CORE_NAME = "product";
	private static final int DEFAULT_ROWS = 20;
	protected RecommendationResponseExecutor responseExecutor;
	protected RecommendationQueryBuilder builder;

	@Override
	public void init(NamedList args) {
		builder = new RecommendationQueryBuilder();
		responseExecutor = new RecommendationResponseExecutor();
		super.init(args);
	}

	@Override
	public void handleRequest(SolrQueryRequest request, SolrQueryResponse response) {
		String productId = request.getParams().get("productid");
		String categoryId = request.getParams().get("catid");
		String cityId = request.getParams().get("cityid");
		String districtId = request.getParams().get("districtid");
		request.setParams(builder.buildRecommendQuery(productId));
		super.handleRequest(request, response);

		List<String> recommendedProductIds = responseExecutor.extractRecommendProductIds(response, request);
		SolrCore productCore = request.getCore().getCoreDescriptor().getCoreContainer().getCore(PRODUCT_CORE_NAME);

		if (recommendedProductIds.size() > 0) {

			request.setParams(builder.buildQueryFromRecommendedIds(recommendedProductIds, cityId, districtId));
			SolrQueryResponse recommendResponse = new SolrQueryResponse();

			SolrQueryRequestBase productSolrRequest = new SolrQueryRequestBase(productCore,
					builder.buildQueryFromRecommendedIds(recommendedProductIds, cityId, districtId)) {
			};

			productCore.getRequestHandler("/base_recommend").handleRequest(productSolrRequest, recommendResponse);

			SolrDocumentList recommendDocList = responseExecutor.getSolrDocumentList(recommendResponse,
					productSolrRequest);

			if (recommendDocList.size() >= DEFAULT_ROWS) {
				responseExecutor.writeRecommendResult(response, recommendDocList);
				request.setParams(builder.setJsonWriter());
			} else {
				final int complementRows = DEFAULT_ROWS - recommendDocList.size();
				SolrQueryRequestBase complementRequest = new SolrQueryRequestBase(productCore,
						builder.buildComplementQuery(productId, categoryId, cityId, districtId, complementRows)) {
				};
				SolrQueryResponse compelementResponse = new SolrQueryResponse();
				productCore.getRequestHandler("/base_recommend").handleRequest(complementRequest, compelementResponse);
				SolrDocumentList compelemtResultDocList = responseExecutor.getSolrDocumentList(compelementResponse,
						complementRequest);
				recommendDocList.addAll(compelemtResultDocList);
				responseExecutor.writeRecommendResult(response, recommendDocList);
				request.setParams(builder.setJsonWriter());
			}
		} else {
			SolrQueryRequestBase complementRequest = new SolrQueryRequestBase(productCore,
					builder.buildComplementQuery(productId, categoryId, cityId, districtId, DEFAULT_ROWS)) {
			};
			SolrQueryResponse compelementResponse = new SolrQueryResponse();
			productCore.getRequestHandler("/base_recommend").handleRequest(complementRequest, compelementResponse);
			SolrDocumentList compelemtResultDocList = responseExecutor.getSolrDocumentList(compelementResponse,
					complementRequest);
			responseExecutor.writeRecommendResult(response, compelemtResultDocList);
			request.setParams(builder.setJsonWriter());
		}
	}
}
