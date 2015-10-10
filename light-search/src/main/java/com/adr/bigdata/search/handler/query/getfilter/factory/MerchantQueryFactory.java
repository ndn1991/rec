/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.factory;

import java.util.List;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.getfilter.bean.AbstractQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.bean.MerchantQueryBean;

/**
 * @author minhvv2 queryType=2
 */
public class MerchantQueryFactory extends QueryFactory {

	@Override
	public AbstractQueryBean create(SolrQueryRequest request) {
		MerchantQueryBean query = new MerchantQueryBean();
		SolrParams param = request.getParams();
		int merchantId = Integer.valueOf(param.get(MERCHANT_ID));
		query.setMerchantId(merchantId);
		try {
			int catId = Integer.valueOf(param.get(CAT_ID));
			query.setCatId(catId);
		} catch (Exception ex) {
			// ignore
		}
		boolean isNew = tryParseBooleanVal(param.get(IS_NEW));
		boolean isPromotion = tryParseBooleanVal(param.get(IS_PROMOTION));
		query.setNew(isNew);
		query.setPromotion(isPromotion);
		query.setPrice(param.get(PRICE));
		List<Integer> brandIds = tryParseListString(param.get(BRAND_ID));
		query.setBrandIds(brandIds);
		return query;
	}
}
