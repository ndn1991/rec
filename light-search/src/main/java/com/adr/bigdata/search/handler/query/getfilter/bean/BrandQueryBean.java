package com.adr.bigdata.search.handler.query.getfilter.bean;

/**
 * @author minhvv2
 */
public class BrandQueryBean extends AbstractQueryBean {
	private int brandId;
	private boolean isPromotion;
	private boolean isNew;
	private String price;
	private int catId;// the cat leaf only

	public int getBrandId() {
		return brandId;
	}

	public void setBrandId(int brandId) {
		this.brandId = brandId;
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

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public int getCatId() {
		return catId;
	}

	public void setCatId(int catId) {
		this.catId = catId;
	}

	/*
	 * debugging purpose
	 */
	@Override
	public String toString() {
		String ret = "price:" + price + " brandId:" + String.valueOf(brandId) + " catId" + String.valueOf(catId);
		return ret;
	}
}
