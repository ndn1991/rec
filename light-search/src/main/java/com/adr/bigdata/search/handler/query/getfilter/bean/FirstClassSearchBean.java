/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.bean;

import java.util.List;

/**
 * @author minhvv2
 *
 */
public class FirstClassSearchBean extends AbstractQueryBean {
	private String keyword;//mandatory	
	private int catId;//mandatory
	private boolean isNew;
	private boolean isPromotion;
	private List<Integer> brandIds;
	private List<Integer> merchantIds;
	private String price;
	private int cityId;
	private AttributeFilterBean attFilter;
	
	public AttributeFilterBean getAttFilter() {
		return attFilter;
	}
	public void setAttFilter(AttributeFilterBean attFilter) {
		this.attFilter = attFilter;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public int getCatId() {
		return catId;
	}
	public void setCatId(int catId) {
		this.catId = catId;
	}
	public boolean isNew() {
		return isNew;
	}
	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}
	public boolean isPromotion() {
		return isPromotion;
	}
	public void setPromotion(boolean isPromotion) {
		this.isPromotion = isPromotion;
	}
	public List<Integer> getBrandIds() {
		return brandIds;
	}
	public void setBrandIds(List<Integer> brandIds) {
		this.brandIds = brandIds;
	}
	public List<Integer> getMerchantIds() {
		return merchantIds;
	}
	public void setMerchantIds(List<Integer> merchantIds) {
		this.merchantIds = merchantIds;
	}
	public String getPrice() {
		return price;
	}
	public void setPrice(String price) {
		this.price = price;
	}
	public int getCityId() {
		return cityId;
	}
	public void setCityId(int cityId) {
		this.cityId = cityId;
	}
		
}
