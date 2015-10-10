/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.bean;

import java.util.List;

/**
 * @author minhvv2 the queryType = 1
 */
public class CatQueryBean extends AbstractQueryBean {
	private int catId;//madatory param
	private int catTypeId;//madatory param
	private List<Integer> merchantIds;
	private List<Integer> brandIds;
	private boolean isPromotion;
	private boolean isNew;
	private int cityId;
	private String prices;
	private AttributeFilterBean attFilter;

	public int getCatId() {
		return catId;
	}

	public void setCatId(int catId) {
		this.catId = catId;
	}

	public int getCatTypeId() {
		return catTypeId;
	}

	public void setCatTypeId(int catTypeId) {
		this.catTypeId = catTypeId;
	}

	public List<Integer> getMerchantIds() {
		return merchantIds;
	}

	public void setMerchantIds(List<Integer> merchantIds) {
		this.merchantIds = merchantIds;
	}

	public List<Integer> getBrandIds() {
		return brandIds;
	}

	public void setBrandIds(List<Integer> brandIds) {
		this.brandIds = brandIds;
	}

	public boolean isPromotion() {
		return isPromotion;
	}

	public void setPromotion(boolean isPromotion) {
		this.isPromotion = isPromotion;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public int getCityId() {
		return cityId;
	}

	public void setCityId(int cityId) {
		this.cityId = cityId;
	}

	public String getPrices() {
		return prices;
	}

	public void setPrices(String prices) {
		this.prices = prices;
	}

	public AttributeFilterBean getAttFilter() {
		return attFilter;
	}

	public void setAttFilter(AttributeFilterBean attFilter) {
		this.attFilter = attFilter;
	}

	/**
	 * for debugging purpose
	 */
	@Override
	public String toString() {
		String ret = "catId:" + String.valueOf(catId) + " catTypeId:" + String.valueOf(catTypeId) + " merchantIds:"
				+ (merchantIds != null ? merchantIds.toString() : "null") + " brandIds:"
				+ (brandIds != null ? brandIds.toString() : "null") + " price:" + (prices != null ? prices : "null")
				+ " isNew:" + String.valueOf(isNew) + " isPromotion:" + String.valueOf(isPromotion) + " attFilter:"
				+ (attFilter != null ? attFilter.toString() : "null");
		return ret;
	}
}
