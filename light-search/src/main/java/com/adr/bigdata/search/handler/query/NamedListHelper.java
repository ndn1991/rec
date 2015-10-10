package com.adr.bigdata.search.handler.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.indexing.db.sql.beans.MerchantBean;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class NamedListHelper {

	public static NamedList convert(Object obj, int numFound) {
		if (obj != null) {
			NamedList ret = null;
			if (obj instanceof BrandBean) {
				ret = new SimpleOrderedMap();
				ret.add("manufactuerId", ((BrandBean) obj).getId());
				ret.add("manufactuerName", ((BrandBean) obj).getName());
				ret.add("numFound", numFound);
			} else if (obj instanceof CategoryBean) {
				ret = new SimpleOrderedMap();
				ret.add("categoryId", ((CategoryBean) obj).getId());
				ret.add("categoryParentId", ((CategoryBean) obj).getParentId());
				ret.add("categoryName", ((CategoryBean) obj).getName());
				ret.add("numFound", numFound);
			} else if (obj instanceof MerchantBean) {
				ret = new SimpleOrderedMap();
				ret.add("merchantId", ((MerchantBean) obj).getId());
				ret.add("merchantName", ((MerchantBean) obj).getName());
				ret.add("numFound", numFound);
			} else {
				throw new IllegalArgumentException("object type `" + obj.getClass().getName()
						+ "` doesn't supportd to convert to named list...");
			}
			return ret;
		}
		return null;
	}

	public static NamedList convert(Object obj, Map<Integer, Integer> refMap) {
		if (obj != null) {
			NamedList ret = null;
			if (obj instanceof MerchantBean) {
				ret = convert(obj, refMap.get(((MerchantBean) obj).getId()));
			} else if (obj instanceof BrandBean) {
				ret = convert(obj, refMap.get(((BrandBean) obj).getId()));
			} else if (obj instanceof CategoryBean) {
				ret = convert(obj, refMap.get(((CategoryBean) obj).getId()));
			} else {
				throw new IllegalArgumentException("object type `" + obj.getClass().getName()
						+ "` doesn't supportd to convert to named list...");
			}
			return ret;
		}
		return null;
	}

	public static List<NamedList> convertList(Collection list, Map<Integer, Integer> numFounds) {
		if (list != null) {
			List<NamedList> ret = new ArrayList<NamedList>();
			for (Object obj : list) {
				ret.add(convert(obj, numFounds));
			}
			return ret;
		}
		return null;
	}

	public static List<NamedList> convertMap(Map<Object, Integer> bean2Numfound) {
		if (bean2Numfound != null) {
			List<NamedList> ret = new ArrayList<NamedList>();
			for (Entry<Object, Integer> entry : bean2Numfound.entrySet()) {
				ret.add(convert(entry.getKey(), entry.getValue()));
			}
			return ret;
		}
		return null;
	}
}
