/**
 * 
 */
package com.adr.bigdata.search.handler.db.sql.models;

import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.search.handler.HazelcastClientAdapter;
import com.adr.bigdata.search.handler.utils.CacheFields;
import com.hazelcast.core.IMap;

/**
 * @author minhvv2
 *
 */
public class CategoryModel extends AbstractModel {

	public CategoryBean getCategory(int catId) {
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
