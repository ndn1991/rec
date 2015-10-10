/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.factory;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.getfilter.bean.AbstractQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.bean.BrandQueryBean;

/**
 * @author minhvv2 queryType=3
 */
public class BrandQueryFactory extends QueryFactory {
	@Override
	public AbstractQueryBean create(SolrQueryRequest request) {
		BrandQueryBean query = new BrandQueryBean();
		SolrParams param = request.getParams();
		try {
			int brandId = Integer.valueOf(param.get(BRAND_ID));//mandatory params
			query.setBrandId(brandId);
		} catch (NumberFormatException ex) {
			getLogger().error("error parsing param at BrandQueryFactory...{}", ex.getMessage());
			throw new RuntimeException("wrong brandListing param at getFilter API....");
		}
		try {
			int catId = Integer.valueOf(param.get(CAT_ID));
			query.setCatId(catId);
		} catch (Exception ex) {

		}
		boolean isNew = tryParseBooleanVal(param.get(IS_NEW));
		query.setNew(isNew);
		boolean isPromotion = tryParseBooleanVal(param.get(IS_PROMOTION));
		query.setPromotion(isPromotion);		
		query.setPrice(param.get(PRICE));
		return query;
	}

}
