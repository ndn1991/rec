package com.adr.bigdata.search.handler.responsestrategy.bean;

import java.util.Map;

public class FilterModelQueryBean {

	private int categoryId;
	private int filteredCategoryId;
	private int merchantId;
	private Map<Integer, Integer> facetedCategoryIds;
	private Map<Integer, Integer> facetedBrandIds;
	private Map<Integer, Integer> facetedMerchantIds;

	public Map<Integer, Integer> getFacetedMerchantIds() {
		return facetedMerchantIds;
	}

	public void setFacetedMerchantIds(Map<Integer, Integer> facetedMerchantIds) {
		this.facetedMerchantIds = facetedMerchantIds;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getFilteredCategoryId() {
		return filteredCategoryId;
	}

	public void setFilteredCategoryId(int filteredCategoryId) {
		this.filteredCategoryId = filteredCategoryId;
	}

	public int getMerchantId() {
		return merchantId;
	}

	public void setMerchantId(int merchantId) {
		this.merchantId = merchantId;
	}

	public Map<Integer, Integer> getFacetedCategoryIds() {
		return facetedCategoryIds;
	}

	public void setFacetedCategoryIds(Map<Integer, Integer> facetedCategoryIds) {
		this.facetedCategoryIds = facetedCategoryIds;
	}

	public Map<Integer, Integer> getFacetedBrandIds() {
		return facetedBrandIds;
	}

	public void setFacetedBrandIds(Map<Integer, Integer> facetedBrandIds) {
		this.facetedBrandIds = facetedBrandIds;
	}

}
