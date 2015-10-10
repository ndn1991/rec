/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.strategy;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

/**
 * @author minhvv2
 *
 */
public class FeaturedFilter extends AbstractFilterStrategy {

	protected static final long IS_NEW_TIME_WINDOWS = 604800000;// 7 days

	@Override
	public void doFilter(Object... data) {
		assert data.length >= 2;
		Boolean isNew = (Boolean) data[0];
		Boolean isPromotion = (Boolean) data[1];
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[2];
		String now = String.valueOf(System.currentTimeMillis());
		String promotionQuery = "is_promotion_mapping:true AND is_promotion:true AND start_time_discount:[* TO " + now
				+ "] AND finish_time_discount:[" + now + " TO *]";
		String isNewQuery = "create_time:[ " + String.valueOf(System.currentTimeMillis() - IS_NEW_TIME_WINDOWS)
				+ " TO *]";
		String tag = "{!tag=featured}";
		// both
		if (isPromotion && isNew) {
			solrParams.add(CommonParams.FQ, "(" + promotionQuery + ") OR " + isNewQuery);
			return;
		} else if (isPromotion) {
			solrParams.add(CommonParams.FQ, tag + promotionQuery);
			setExcludeFeatured(solrParams);
			return;
		} else if (isNew) {
			solrParams.add(CommonParams.FQ, tag + isNewQuery);
			setExcludeFeatured(solrParams);
			return;
		}
	}

	/**
	 * minhvv2: isNewQuery is put before isPromotionQuery, just like the order
	 * they appear on frontEndPage
	 */
	@Override
	public void doFacet(Object... data) {
		assert data.length >= 1;
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[0];
		String now = String.valueOf(System.currentTimeMillis());
		String isNewQuery = "create_time:[ " + String.valueOf(System.currentTimeMillis() - IS_NEW_TIME_WINDOWS)
				+ " TO *]";
		String promotionQuery = "is_promotion_mapping:true AND is_promotion:true AND start_time_discount:[* TO " + now
				+ "] AND finish_time_discount:[" + now + " TO *]";
		solrParams.set("facet.query", isNewQuery, promotionQuery);
	}

	private void setExcludeFeatured(ModifiableSolrParams solrParams) {
		solrParams.remove("facet.query");
		String now = String.valueOf(System.currentTimeMillis());
		String isNewQuery = "{!ex=featured}create_time:[ "
				+ String.valueOf(System.currentTimeMillis() - IS_NEW_TIME_WINDOWS) + " TO *]";
		String promotionQuery = "{!ex=featured}is_promotion_mapping:true AND is_promotion:true AND start_time_discount:[* TO "
				+ now + "] AND finish_time_discount:[" + now + " TO *]";
		solrParams.set("facet.query", isNewQuery, promotionQuery);
	}
}