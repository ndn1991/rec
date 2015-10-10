/**
 * 
 */
package com.adr.bigdata.search.handler.query.frontend;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.firstclassfunction.Function;
import com.adr.bigdata.search.handler.utils.SolrQueryRequestHelper;
import com.google.api.client.util.Strings;
import com.google.common.base.Joiner;

/**
 * @author minhvv2
 *
 */
public class ComboCreationQueryBuilder extends FrontEndQueryBuilder {
	private Function<String, String> transformer = new Function<String, String>() {
		@Override
		public String apply(String t) {
			return t;
		}
	};

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		ModifiableSolrParams solrParams = (ModifiableSolrParams) super.buildSolrQuery(request);
		String productIdFqQuery = Joiner.on(" OR product_id:").join(
				SolrQueryRequestHelper.extractListParams(request, "productIds", ",", transformer));
		if (!Strings.isNullOrEmpty(productIdFqQuery)) {
			solrParams.add(CommonParams.FQ, "product_id:" + productIdFqQuery);
		}
		String productItemIdFqQuery = Joiner.on(" OR product_item_id:").join(
				SolrQueryRequestHelper.extractListParams(request, "productItemIds", ",", transformer));
		if (!Strings.isNullOrEmpty(productItemIdFqQuery)) {
			solrParams.add(CommonParams.FQ, "product_item_id:" + productItemIdFqQuery);
		}
		return solrParams;
	}

}
