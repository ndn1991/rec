package com.adr.bigdata.search.handler.facetextractor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;

import com.nhb.common.Loggable;

public class FacetResultExtractor implements Loggable {

	private final NamedList facetCounts;

	public FacetResultExtractor(NamedList facetCounts) {
		this.facetCounts = facetCounts;
	}

	public Map<Integer, Integer> generateFacetedCategoryIds() {
		if (facetCounts == null) {
			return Collections.EMPTY_MAP;
		}
		NamedList facetField = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
		if (facetField == null) {
			return Collections.EMPTY_MAP;
		}
		NamedList categoryIds = (NamedList) facetField.get(FacetConstant.CATEGORY_ID_FACET);
		if (categoryIds == null) {
			return Collections.EMPTY_MAP;
		}

		Map<Integer, Integer> facetedCategoryIds = new HashMap<>();
		Iterator<Map.Entry<String, Object>> it = categoryIds.iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> next = it.next();
			Integer categoryId = Integer.parseInt(next.getKey());
			Integer numFound = (Integer) next.getValue();
			facetedCategoryIds.put(categoryId, numFound);
		}

		if (facetedCategoryIds.isEmpty())
			return Collections.EMPTY_MAP;
		return facetedCategoryIds;
	}

	public Map<Integer, Integer> generateFacetedBrands() {
		if (facetCounts == null) {
			return Collections.EMPTY_MAP;
		}
		NamedList facetField = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
		if (facetField == null) {
			return Collections.EMPTY_MAP;
		}
		NamedList brandIds = (NamedList) facetField.get(FacetConstant.BRAND_ID_FACET);
		if (brandIds == null) {
			return Collections.EMPTY_MAP;
		}

		Map<Integer, Integer> factedBrandIds = new HashMap<>();
		Iterator<Map.Entry<String, Object>> it = brandIds.iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> next = it.next();
			Integer manufactuerId = Integer.parseInt(next.getKey());
			Integer numFound = (Integer) next.getValue();
			factedBrandIds.put(manufactuerId, numFound);
		}
		if (factedBrandIds.isEmpty())
			return Collections.EMPTY_MAP;
		return factedBrandIds;
	}

	public Map<Integer, Integer> generateFacetedMerchants() {
		if (facetCounts == null) {
			return Collections.EMPTY_MAP;
		}
		NamedList facetField = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
		if (facetField == null) {
			return Collections.EMPTY_MAP;
		}
		NamedList merchantIds = (NamedList) facetField.get(FacetConstant.MERCHANT_ID_FACET);
		if (merchantIds == null) {
			return Collections.EMPTY_MAP;
		}

		Map<Integer, Integer> factedMerchantIds = new HashMap<>();
		Iterator<Map.Entry<String, Object>> it = merchantIds.iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> next = it.next();
			Integer merchantId = Integer.parseInt(next.getKey());
			Integer numFound = (Integer) next.getValue();
			factedMerchantIds.put(merchantId, numFound);
		}
		if (factedMerchantIds.isEmpty())
			return Collections.EMPTY_MAP;

		return factedMerchantIds;
	}

	public List<NamedList> createListCity() {
		List<NamedList> facetedCities = new ArrayList<>();
		if (facetCounts == null) {
			return facetedCities;
		}
		NamedList facetField = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
		if (facetField == null) {
			return facetedCities;
		}

		NamedList facetedCitiesNamedList = (NamedList) facetField.get(FacetConstant.CITY_ID_FACET);
		if (facetedCitiesNamedList == null) {
			return facetedCities;
		}

		for (int i = 0; i < facetedCitiesNamedList.size(); i++) {
			NamedList cityBean = new SimpleOrderedMap();
			cityBean.add("cityId", facetedCitiesNamedList.getName(i));
			cityBean.add("numFound", facetedCitiesNamedList.getVal(i));
			facetedCities.add(cityBean);
		}
		return facetedCities;

	}

	public List<NamedList> createListFeatured() {
		
		List<NamedList> lstFeatured = new ArrayList<>();
		if (facetCounts == null) {
			return lstFeatured;
		}
		NamedList facetQuery = (NamedList) facetCounts.get(FacetConstant.FACET_QUERIES);
		if (facetQuery == null) {
			return lstFeatured;
		}

		Integer newArrived = (Integer) facetQuery.getVal(1);
		Integer promotioned = (Integer) facetQuery.getVal(0);

		lstFeatured.add(createFeatured("isPromotion", promotioned));
		lstFeatured.add(createFeatured("newArrival", newArrived));

		return lstFeatured;
	}

	private NamedList createFeatured(String featuredName, Integer numFound) {
		NamedList ret = new SimpleOrderedMap<>();
		ret.add("featuredName", featuredName);
		ret.add("numFound", numFound);
		return ret;
	}

	private NamedList createPromotion(Boolean isPromotion, String promotionName, Integer numFound) {
		NamedList ret = new SimpleOrderedMap();
		ret.add("isPromotion", isPromotion);
		ret.add("promotionName", promotionName);
		ret.add("numFound", numFound);
		return ret;
	}
}
