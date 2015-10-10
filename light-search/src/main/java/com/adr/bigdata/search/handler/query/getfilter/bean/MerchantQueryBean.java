/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.bean;

import java.util.List;

/**
 * @author minhvv2
 *
 */
public class MerchantQueryBean extends AbstractQueryBean {
	private int merchantId;
	private boolean isPromotion;
	private boolean isNew;
	private int catId;//cat leaf only
	private String price;
	private List<Integer> brandIds;

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

	public List<Integer> getBrandIds() {
		return brandIds;
	}

	public void setBrandIds(List<Integer> brandIds) {
		this.brandIds = brandIds;
	}

	public int getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}

	public int getCatId() {
		return catId;
	}

	public void setCatId(int catId) {
		this.catId = catId;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	/*
	 * debugging purpose
	 */
	@Override
	public String toString() {
		String ret = "merchantId:" + String.valueOf(merchantId) + " isNew" + String.valueOf(isNew) + " brandIds:"
				+ brandIds.toString();
		return ret;
	}
}
