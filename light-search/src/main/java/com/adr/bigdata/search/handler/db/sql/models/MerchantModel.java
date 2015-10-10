package com.adr.bigdata.search.handler.db.sql.models;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.adr.bigdata.indexing.db.sql.beans.MerchantBean;
import com.adr.bigdata.search.handler.HazelcastClientAdapter;
import com.adr.bigdata.search.handler.utils.CacheFields;
import com.hazelcast.core.IMap;

public class MerchantModel extends AbstractModel {
	public final static int DISABLE_MERCHANT_ID = -1;

	public Map<Integer, MerchantBean> getAllMerchant() {
		IMap<Integer, MerchantBean> id2Merchant = null;
		try {
			id2Merchant = HazelcastClientAdapter.getMap(CacheFields.MERCHANT);
		} catch (Exception e) {
			getLogger().error("error getting brands from cache", e);
		}
		if (id2Merchant == null) {
			return null;
		}
		Map<Integer, MerchantBean> ret = new HashMap<Integer, MerchantBean>();
		Set<Integer> keySets = id2Merchant.keySet();
		for (Integer merchantId : keySets) {
			ret.put(merchantId, id2Merchant.get(merchantId));
		}
		return ret;
	}

	public MerchantBean getMerchant(int mcId) throws Exception {

		MerchantBean merchantBean = null;
		if (mcId != DISABLE_MERCHANT_ID) {
			try {
				merchantBean = (MerchantBean) HazelcastClientAdapter.getMap(CacheFields.MERCHANT).get(mcId);
			} catch (Exception e) {
				getLogger().error("fail to  get merchant bean map from cache", e);
			}
			//			if (merchantBean == null) {
			//				try (MerchantDAO merchantDAO = getDbAdapter().openDAO(MerchantDAO.class)) {
			//					getLogger().info("retry get merchant from db: + {}", mcId);
			//					merchantBean = merchantDAO.getMerchant(mcId);
			//					getLogger().info("geted from db: {}", merchantBean);
			//				}
			//			}
		}
		return merchantBean;
	}

	public Collection<MerchantBean> getAllMerchants(Set<Integer> setMerchantIds) throws Exception {

		if (setMerchantIds == null || setMerchantIds.isEmpty()) {
			return null;
		}

		IMap<Integer, MerchantBean> id2Merchant = null;

		try {
			id2Merchant = HazelcastClientAdapter.getMap(CacheFields.MERCHANT);
		} catch (Exception e) {
			getLogger().error("fail to  get merchant bean map from cache", e);
		}

		Collection<MerchantBean> merchantBeans = null;
		if (id2Merchant != null) {
			merchantBeans = id2Merchant.getAll(setMerchantIds).values();
		}

		//		if (merchantBeans == null) {
		//			try (MerchantDAO dao = getDbAdapter().openDAO(MerchantDAO.class)) {
		//				getLogger().info("FacetedCategoryModel - hit to db");
		//				merchantBeans = dao.getMerchants(setMerchantIds);
		//				getLogger().info("got from db: {}", merchantBeans);
		//			}
		//		}
		return merchantBeans;

	}
}
