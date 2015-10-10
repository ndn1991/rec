/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.query.frontend;

import com.adr.bigdata.search.handler.query.frontend.FrontEndQuery.Param;
import com.adr.bigdata.search.handler.utils.ConvertStringToString;
import com.google.common.base.Strings;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

/**
 *
 * @author Tong Hoang Anh
 */
public class SuggestionQuery {

	private String keyword = Params.DEFAULT_KEYWORD;
	private String userId = Params.DEFAULT_USER;
	private String suggestionSize = Params.DEFAULT_SUGGESTION_SIZE;
	private int suggestedCategorySize = Params.DEFALUT_SUGGESTED_CATEGORY_SIZE;
    private String train;
    private String cityId;
    private String districtId;
    
    

	//no public constructor
	private SuggestionQuery() {
	}

	public static SuggestionQuery createQuery(SolrQueryRequest req) {
		SuggestionQuery suggestionQuery = new SuggestionQuery();
		SolrParams param = req.getParams();
		suggestionQuery.setKeyword(param.get(Params.KEYWORD));
		suggestionQuery.setUserId(param.get(Params.USER_ID));
                suggestionQuery.setTrain(param.get(Params.TRAIN));
        suggestionQuery.setCityId(param.get(Params.CITY_ID));
        suggestionQuery.setDistrictId(param.get(Params.DISTRICT_ID));
		return suggestionQuery;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		if (!Strings.isNullOrEmpty(keyword)) {
			this.keyword = ConvertStringToString.decodeSumaryToNormal(keyword);
		}
	}
        
        public void setTrain(String train){
            this.train = train;
        }
        
        public String getTrain(){
            if (Strings.isNullOrEmpty(train)) {
                return Params.DEFAULT_TRAIN;
            }
            return this.train;
        }

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		if (!Strings.isNullOrEmpty(userId)) {
			this.userId = userId;
		}
	}

	public String getSuggestionSize() {
		return suggestionSize;
	}

	public void setSuggestionSize(String suggestionSize) {
		if (!Strings.isNullOrEmpty(suggestionSize)) {
			this.suggestionSize = suggestionSize;
		}
	}

	public int getSuggestedCategorySize() {
		return suggestedCategorySize;
	}

	public void setSuggestedCategorySize(String suggestedCategorySize) {
		try {
			this.suggestedCategorySize = Integer.parseInt(suggestedCategorySize);
		} catch (Exception e) {
			throw new IllegalArgumentException("suggested category size must be a number", e);
		}
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	public String getDistrictId() {
		return districtId;
	}

	public void setDistrictId(String districtId) {
		this.districtId = districtId;
	}




	public static class Params {

		public final static String KEYWORD = "keyword";
		public final static String USER_ID = "userid";
        public final static String TRAIN = "train";
        public final static String GET_DATA = "getdata";
        public final static String DEFAULT_TRAIN = "false";
		private final static String DEFAULT_USER = "0";
		private final static String DEFAULT_KEYWORD = "*:*";
		private final static String DEFAULT_SUGGESTION_SIZE = "10";
		private final static int DEFALUT_SUGGESTED_CATEGORY_SIZE = 5;
		private final static String CITY_ID = "cityid";
		private final static String DISTRICT_ID = "districtid";
	}
}
