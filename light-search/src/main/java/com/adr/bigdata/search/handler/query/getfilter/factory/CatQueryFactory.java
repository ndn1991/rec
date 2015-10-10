/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.factory;

import java.util.List;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.getfilter.bean.AbstractQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.bean.AttributeFilterBean;
import com.adr.bigdata.search.handler.query.getfilter.bean.CatQueryBean;

/**
 * @author minhvv2
 *
 */
public class CatQueryFactory extends QueryFactory {
	/*
	 * create category query bean from solr request
	 */
	@Override
	public AbstractQueryBean create(SolrQueryRequest request) {
		CatQueryBean queryBean = new CatQueryBean();
		SolrParams param = request.getParams();
		int catId = Integer.valueOf(param.get(CAT_ID));// mandatory params
		queryBean.setCatId(catId);
		int catTypeId = Integer.valueOf(param.get(CAT_TYPE));//mandatory params
		queryBean.setCatTypeId(catTypeId);
		try {
			int cityId = Integer.valueOf(param.get(CITY_ID));
			queryBean.setCityId(cityId);
		} catch (NumberFormatException ex) {

		}
		List<Integer> merchantIds = tryParseListString(param.get(MERCHANT_ID));
		queryBean.setMerchantIds(merchantIds);
		List<Integer> brandIds = tryParseListString(param.get(BRAND_ID));
		queryBean.setBrandIds(brandIds);
		queryBean.setPrices(param.get(PRICE));
		AttFilterQueryFactory attFactory = new AttFilterQueryFactory();
		queryBean.setAttFilter((AttributeFilterBean) attFactory.create(request));
		boolean isNew = tryParseBooleanVal(param.get(IS_NEW));
		queryBean.setNew(isNew);
		boolean isPromotion = tryParseBooleanVal(param.get(IS_PROMOTION));
		queryBean.setPromotion(isPromotion);
		return queryBean;
	}

}
