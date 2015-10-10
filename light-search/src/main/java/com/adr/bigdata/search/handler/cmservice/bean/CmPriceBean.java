package com.adr.bigdata.search.handler.cmservice.bean;

import com.google.api.client.util.Key;

public class CmPriceBean {
	@Key("ProductItemId")
	private int productItemId;
	@Key("OriginalPrice")
	private double originalPrice;
	@Key("PercentDiscount")
	private double percentDiscount;

	public Integer getProductItemId() {
		return productItemId;
	}

	public double getOriginalPrice() {
		return originalPrice;
	}

	public double getPercentDiscount() {
		return percentDiscount;
	}

}