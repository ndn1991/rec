package com.adr.bigdata.search.handler.db.sql.models;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.adr.bigdata.search.handler.HazelcastClientAdapter;
import com.adr.bigdata.search.handler.utils.CacheFields;
import com.hazelcast.core.IMap;

public class BrandModel extends AbstractModel {

	public Map<Integer, BrandBean> getAllBrands() {
		IMap<Integer, BrandBean> id2Brand = null;
		try {
			id2Brand = HazelcastClientAdapter.getMap(CacheFields.BRAND);
		} catch (Exception e) {
			getLogger().error("error getting brands from cache", e);
		}
		if (id2Brand == null) {
			return null;
		}
		Map<Integer, BrandBean> ret = new HashMap<Integer, BrandBean>();
		Set<Integer> keySets = id2Brand.keySet();
		for (Integer brandId : keySets) {
			ret.put(brandId, id2Brand.get(brandId));
		}
		return ret;
	}

	public Collection<BrandBean> getBrands(Set<Integer> brandIds) throws Exception {
		if (brandIds.isEmpty())
			return null;

		IMap<Integer, BrandBean> id2Brand = null;
		try {
			id2Brand = HazelcastClientAdapter.getMap(CacheFields.BRAND);
		} catch (Exception e) {
			getLogger().error("fail to get brands from cache", e);
		}

		if (brandIds != null && !brandIds.isEmpty()) {
			Collection<BrandBean> brandBeans = null;

			if (id2Brand != null) {
				brandBeans = id2Brand.getAll(brandIds).values();
			}

			if (brandBeans == null || brandBeans.isEmpty()) {
				//				try (BrandDAO dao = getDbAdapter().openDAO(BrandDAO.class)) {
				//					getLogger().info("BrandModel - hit to db");
				//					brandBeans = dao.getBrands(brandIds);
				//					getLogger().info("geted from db: {}", brandBeans);
				//				}
				//do nothing
			}
			return brandBeans;
		} else {
			return Collections.emptyList();
		}
	}

	public BrandBean getBrand(int brandId) {
		if (brandId < 0) {
			return null;
		}

		IMap<Integer, BrandBean> id2Brand = null;
		try {
			id2Brand = HazelcastClientAdapter.getMap(CacheFields.BRAND);
		} catch (Exception e) {
			getLogger().error("fail to get brands from cache", e);
		}
		if (id2Brand != null) {
			return id2Brand.get(brandId);
		}
		return null;
	}
}
