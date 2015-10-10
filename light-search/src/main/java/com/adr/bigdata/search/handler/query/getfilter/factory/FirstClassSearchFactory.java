/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.factory;

import java.util.List;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.getfilter.bean.AbstractQueryBean;
import com.adr.bigdata.search.handler.query.getfilter.bean.AttributeFilterBean;
import com.adr.bigdata.search.handler.query.getfilter.bean.FirstClassSearchBean;

/**
 * @author minhvv2
 *
 */
public class FirstClassSearchFactory extends QueryFactory {

	/**
	 * generate first class keyword query
	 */
	@Override
	public AbstractQueryBean create(SolrQueryRequest request) {
		FirstClassSearchBean query = new FirstClassSearchBean();
		SolrParams param = request.getParams();
		String keyword = param.get(KEYWORD);
		query.setKeyword(keyword);//mandatory param
		int catId = Integer.valueOf(param.get(CAT_ID));
		query.setCatId(catId);//mandatory param
		query.setPrice(param.get(PRICE));
		try {
			int cityId = Integer.valueOf(param.get(CITY_ID));
			query.setCityId(cityId);
		} catch (Exception ex) {
			//do nothing
		}

		AttFilterQueryFactory attFactory = new AttFilterQueryFactory();
		query.setAttFilter((AttributeFilterBean) attFactory.create(request));

		boolean isNew = tryParseBooleanVal(param.get(IS_NEW));
		boolean isPromotion = tryParseBooleanVal(param.get(IS_PROMOTION));
		query.setNew(isNew);
		query.setPromotion(isPromotion);
		List<Integer> brandIds = tryParseListString(param.get(BRAND_ID));
		List<Integer> merchantIds = tryParseListString(param.get(MERCHANT_ID));
		query.setBrandIds(brandIds);
		query.setMerchantIds(merchantIds);
		return query;
	}

}
