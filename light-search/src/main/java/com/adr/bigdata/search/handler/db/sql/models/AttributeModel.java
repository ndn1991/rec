package com.adr.bigdata.search.handler.db.sql.models;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.adr.bigdata.indexing.db.sql.beans.AttributeValueMeasureUnitDisplayBean;
import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.search.handler.HazelcastClientAdapter;
import com.adr.bigdata.search.handler.utils.CacheFields;
import com.hazelcast.core.IMap;

/**
 * data model: get data from hazelcast. Return null if there's exception
 * connecting hazelcast.
 * 
 * @author minhvv2
 *
 */
public class AttributeModel extends AbstractModel {

	public Map<Integer, Set<AttributeCategoryMappingBean>> getAllAttributeModel() {

		IMap<Integer, CategoryBean> id2Cat = null;
		try {
			id2Cat = HazelcastClientAdapter.getMap(CacheFields.CATEGORY);
		} catch (Exception e) {
			getLogger().error("error getting catIdset from hazelCache...{}", e);
		}
		if (id2Cat == null) {
			return null;
		}
		Set<Integer> allCategories = id2Cat.keySet();
		Map<Integer, Set<AttributeCategoryMappingBean>> allCat2AttMap = new HashMap<>();
		for (Integer catId : allCategories) {
			try {
				allCat2AttMap.put(catId, getAttributesByCat(catId));
			} catch (Exception e) {
				getLogger().error("error getting Attribute set...{}", e);
				// just ignore this one
				continue;
			}
		}

		return allCat2AttMap;
	}

	public Set<AttributeCategoryMappingBean> getAttributesByCat(int catId) throws Exception {
		CategoryBean categoryBean = null;
		IMap<Integer, CategoryBean> id2Cat = null;
		try {
			id2Cat = HazelcastClientAdapter.getMap(CacheFields.CATEGORY);
		} catch (Exception e) {
			getLogger().error("fail to get CATEGORY from cache", e);
		}

		if (id2Cat == null) {
			//			try (CategoryDAO categoryDAO = getDbAdapter().openDAO(CategoryDAO.class)) {
			//				categoryBean = categoryDAO.getCategories(new HashSet<Integer>(catId)).get(0);
			//			}
			return null;
		} else {
			categoryBean = id2Cat.get(catId);
		}

		IMap<Integer, Map<Integer, AttributeCategoryMappingBean>> id2AttCategoryMapping = null;

		try {
			id2AttCategoryMapping = HazelcastClientAdapter.getMap(CacheFields.ATTRIBUTE_CATEGORY_FILTER);
		} catch (Exception ex) {
			getLogger().error("fail to get AttributeCategoryMappingBean from cache", ex);
		}
		if (id2AttCategoryMapping == null) {
			//			try (CategoryDAO categoryDAO = getDbAdapter().openDAO(CategoryDAO.class)) {
			//				return new HashSet<AttributeCategoryMappingBean>(categoryDAO.getAttributeCategoryMappings(catId));
			//			}
			return null;
		} else {
			Collection<Map<Integer, AttributeCategoryMappingBean>> cachedAttCategory = id2AttCategoryMapping.getAll(
					new HashSet<Integer>(categoryBean.getPath())).values();
			return unionAllMapCategory(cachedAttCategory);
		}
	}

	public Map<Integer, AttributeCategoryMappingBean> getAttributesMapByCat(int catId) throws Exception {
		CategoryBean categoryBean = null;
		IMap<Integer, CategoryBean> id2Cat = null;
		try {
			id2Cat = HazelcastClientAdapter.getMap(CacheFields.CATEGORY);
		} catch (Exception e) {
			getLogger().error("fail to get CATEGORY from cache", e);
		}

		if (id2Cat == null) {
			//			try (CategoryDAO categoryDAO = getDbAdapter().openDAO(CategoryDAO.class)) {
			//				categoryBean = categoryDAO.getCategories(new HashSet<Integer>(catId)).get(0);
			//			}
			return null;
		} else {
			categoryBean = id2Cat.get(catId);
		}

		IMap<Integer, Map<Integer, AttributeCategoryMappingBean>> id2AttCategoryMapping = null;

		try {
			id2AttCategoryMapping = HazelcastClientAdapter.getMap(CacheFields.ATTRIBUTE_CATEGORY_FILTER);
		} catch (Exception ex) {
			getLogger().error("fail to get AttributeCategoryMappingBean from cache", ex);
		}
		if (id2AttCategoryMapping == null) {
			//			try (CategoryDAO categoryDAO = getDbAdapter().openDAO(CategoryDAO.class)) {
			//				return CollectionUtils.list2Map(categoryDAO.getAttributeCategoryMappings(catId), "getAttributeId");
			//			}
			return null;
		} else {
			Collection<Map<Integer, AttributeCategoryMappingBean>> cachedAttCategory = id2AttCategoryMapping.getAll(
					new HashSet<Integer>(categoryBean.getPath())).values();

			Map<Integer, AttributeCategoryMappingBean> result = new HashMap<Integer, AttributeCategoryMappingBean>();

			for (Map<Integer, AttributeCategoryMappingBean> mapElement : cachedAttCategory) {
				for (Entry<Integer, AttributeCategoryMappingBean> entry : mapElement.entrySet()) {
					if (isVisibleAttributeType(entry.getValue())) {
						result.put(entry.getKey(), entry.getValue());
					}
				}

			}

			return result;
		}
	}

	/**
	 * 
	 * @param mergedAttValLst
	 *            : collection of String that joined between attId and AttVal.
	 *            For example attId=142, attVal=150.0, the joined String is:
	 *            142_150.0
	 * @return
	 */
	public Map<String, String> getDisplayAttribute(Map<String, String> val2joinedAttIdValue) {
		IMap<String, AttributeValueMeasureUnitDisplayBean> val2DisplayAtt = null;
		try {
			val2DisplayAtt = HazelcastClientAdapter.getMap(CacheFields.DISPLAY_UNIT);
		} catch (Exception e) {
			getLogger().error("fail to get DISPLAY_UNIT from cache", e);
		}
		if (val2DisplayAtt == null) {
			return null;
		}
		Map<String, AttributeValueMeasureUnitDisplayBean> hazelResult = val2DisplayAtt.getAll(new HashSet(
				val2joinedAttIdValue.values()));
		Map<String, String> invertedVal2joinedAttIdValue = new HashMap<>();
		for (Entry<String, String> invertedEntry : val2joinedAttIdValue.entrySet()) {
			invertedVal2joinedAttIdValue.put(invertedEntry.getValue(), invertedEntry.getKey());
		}
		Map<String, String> result = new HashMap<String, String>();
		for (Entry<String, AttributeValueMeasureUnitDisplayBean> entry : hazelResult.entrySet()) {
			String joinedAttIdValue = entry.getKey();
			String value = invertedVal2joinedAttIdValue.getOrDefault(joinedAttIdValue, entry.getValue().getValue());
			Double doubleVal = null;
			try {
				doubleVal = Double.valueOf(value);
			} catch (NumberFormatException ex) {
				continue;
			}
			double ratio = entry.getValue().getDisplayRatio();
			result.put(value, transform(doubleVal, ratio, entry.getValue().getDisplayUnitName()));
		}
		return result;
	}

	private String transform(Double doubleVal, double ratio, String unit) {
		Integer accuracy = Integer.valueOf(System.getProperty("getfilter.attribute.doubleaccuracy", "3"));
		double displayVal = doubleVal;
		if (ratio > 0) {
			displayVal = doubleVal / ratio;
		}
		String strAccuracy = StringUtils.repeat("#", accuracy);
		DecimalFormat format = new DecimalFormat("0." + strAccuracy);
		String result = format.format(displayVal);
		return (result + " " + unit.trim());
	}

	/**
	 * only attribute type = 2 or 4 are visible
	 * 
	 * @param attributeBean
	 * @return
	 */
	private boolean isVisibleAttributeType(AttributeCategoryMappingBean attributeBean) {
		return attributeBean.getAttributeType() == 2 || attributeBean.getAttributeType() == 4;
	}

	private Set<AttributeCategoryMappingBean> unionAllMapCategory(
			Collection<Map<Integer, AttributeCategoryMappingBean>> cachedAttCategory) {
		Set<AttributeCategoryMappingBean> result = new HashSet<AttributeCategoryMappingBean>();
		for (Map<Integer, AttributeCategoryMappingBean> attId2Bean : cachedAttCategory) {
			result.addAll(attId2Bean.values());
		}
		return result;
	}
}
