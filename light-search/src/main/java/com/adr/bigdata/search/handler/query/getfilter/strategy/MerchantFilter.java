/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.strategy;

import java.util.List;
import java.util.stream.Stream;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.adr.bigdata.search.handler.utils.Constant;
import com.adr.bigdata.search.handler.utils.SolrParamHelper;
import com.google.common.base.Joiner;

/**
 * @author minhvv2
 *
 */
public class MerchantFilter extends AbstractFilterStrategy {

	@Override
	public void doFilter(Object... data) {
		assert data.length >= 2;
		List<Integer> merchantId = (List<Integer>) data[0];
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[1];
		if (merchantId != null) {
			Stream<String> lstMerchants = merchantId.stream().map(merchant -> "merchant_id:" + merchant.toString());
			solrParams.add(CommonParams.FQ, "{!tag=merchant}" + Joiner.on(" OR ").join(lstMerchants.iterator()));
			SolrParamHelper.writeTaggedFacet(solrParams, "merchant_id_facet", "{!ex=merchant}merchant_id_facet");
		}
	}

	@Override
	public void doFacet(Object... data) {
		assert data.length >= 1;
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[0];
		solrParams.add("facet.field", "merchant_id_facet");
		solrParams.set("f.merchant_id_facet.facet.limit", Constant.FACET_LIMIT);
	}
}
