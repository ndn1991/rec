/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.vo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

/**
 *
 * @author ndn
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class CategoryTreeVO {

	public final int TYPE_NON = 0;
	public final int TYPE_BOLD = 1;
	public final int TYPE_ARROW = 2;

	private int type;
	private int categoryId;
	private boolean isLeaf;
	private int categoryParentId;
	private String categoryName;
	private List<CategoryTreeVO> listCategorySub = Collections.emptyList();

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public boolean isIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public int getCategoryParentId() {
		return categoryParentId;
	}

	public void setCategoryParentId(int categoryParentId) {
		this.categoryParentId = categoryParentId;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public List<CategoryTreeVO> getListCategorySub() {
		return listCategorySub;
	}

	public void setListCategorySub(List<CategoryTreeVO> listCategorySub) {
		this.listCategorySub = listCategorySub;
	}
		

	public NamedList toNamedList() {
		NamedList categoryTree = new SimpleOrderedMap();
		categoryTree.add("type", type);
		categoryTree.add("categoryId", categoryId);
		categoryTree.add("isLeaf", isLeaf);
		categoryTree.add("categoryParentId", categoryParentId);
		categoryTree.add("categoryName", categoryName);
		List<NamedList> listSubNamedList = new ArrayList<>();
		for (CategoryTreeVO child : listCategorySub) {
			listSubNamedList.add(child.toNamedList());
		}
		categoryTree.add("listCategorySub", listSubNamedList);
		return categoryTree;
	}
}
