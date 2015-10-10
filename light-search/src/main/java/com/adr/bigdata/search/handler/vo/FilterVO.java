/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.vo;

import java.util.Collection;
import java.util.List;

import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.indexing.db.sql.beans.MerchantBean;
import com.adr.bigdata.search.handler.query.AttributeFilter;

/**
 *
 * @author Tong Hoang Anh
 */
public class FilterVO {

	private List<AttributeFilter> attFilters;
	private Collection<CategoryBean> categories;
	private Collection<BrandBean> brands;
	private CategoryTreeVO categoryTree;
    private MerchantBean merchantBean;

    public MerchantBean getMerchantBean() {
		return merchantBean;
	}

	public void setMerchantBean(MerchantBean merchantBean) {
		this.merchantBean = merchantBean;
	}

	public List<AttributeFilter> getAttFilters() {
		return attFilters;
	}

	public void setAttFilters(List<AttributeFilter> attFilters) {
		this.attFilters = attFilters;
	}

	public Collection<CategoryBean> getCategories() {
		return categories;
	}

	public void setCategories(Collection<CategoryBean> categories) {
		this.categories = categories;
	}

	public Collection<BrandBean> getBrands() {
		return brands;
	}

	public void setBrands(Collection<BrandBean> brands) {
		this.brands = brands;
	}

	public CategoryTreeVO getCategoryTree() {
		return categoryTree;
	}

	public void setCategoryTree(CategoryTreeVO categoryTree) {
		this.categoryTree = categoryTree;
	}

}
