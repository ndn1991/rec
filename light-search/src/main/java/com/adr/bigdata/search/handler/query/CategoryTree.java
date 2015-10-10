package com.adr.bigdata.search.handler.query;

import java.util.Collections;
import java.util.List;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

/**
 *
 * @author Tong Hoang Anh
 */

@SuppressWarnings({ "rawtypes", "unchecked", "unused" })
public class CategoryTree {

	private int type;
	private int categoryId;
	private boolean isLeaf;
	private int categoryParentId;
	private String categoryName;
	private List<CategoryTree> listCategorySub = Collections.EMPTY_LIST;

	public NamedList toNamedList() {
		NamedList categoryTree = new SimpleOrderedMap();
		categoryTree.add("type", type);
		categoryTree.add("categoryId", categoryId);
		categoryTree.add("categoryParentId", categoryParentId);
		categoryTree.add("categoryName", categoryName);
		for (CategoryTree child : listCategorySub) {
			categoryTree.add("listCategorySub", child.toNamedList());
		}
		return categoryTree;
	}
}
