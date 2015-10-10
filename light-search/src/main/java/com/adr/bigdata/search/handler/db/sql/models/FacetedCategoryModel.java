package com.adr.bigdata.search.handler.db.sql.models;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.search.handler.HazelcastClientAdapter;
import com.adr.bigdata.search.handler.utils.CacheFields;
import com.hazelcast.core.IMap;

public class FacetedCategoryModel extends AbstractModel {

	public Collection<CategoryBean> getFacetCategories(Set<Integer> facetedCatIds) throws Exception {

		if (facetedCatIds.isEmpty())
			return null;

		IMap<Integer, CategoryBean> id2Cat = null;
		try {
			id2Cat = HazelcastClientAdapter.getMap(CacheFields.CATEGORY);
		} catch (Exception e) {
			getLogger().error("Fail to get category map from cache", e);
		}

		if (facetedCatIds != null && !facetedCatIds.isEmpty()) {
			Collection<CategoryBean> categoryBeans = null;
			if (id2Cat != null) {
				categoryBeans = id2Cat.getAll(facetedCatIds).values();
			}

			return categoryBeans;
		} else {
			return Collections.emptyList();
		}
	}

	public CategoryBean getCategoryBean(int catId) {
		IMap<Integer, CategoryBean> id2Cat = null;
		CategoryBean categoryBean = null;
		try {
			id2Cat = HazelcastClientAdapter.getMap(CacheFields.CATEGORY);
		} catch (Exception e) {
			getLogger().error("Fail to get category map from cache", e);
		}

		if (id2Cat != null) {
			categoryBean = id2Cat.get(catId);
		}
		return categoryBean;
	}

}
