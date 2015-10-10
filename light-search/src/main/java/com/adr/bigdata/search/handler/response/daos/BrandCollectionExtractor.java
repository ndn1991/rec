package com.adr.bigdata.search.handler.response.daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.nhb.common.Loggable;

public class BrandCollectionExtractor implements Loggable {
	private static final int FILTER_MAX_MERCHANT = 100;

	/**
	 * the brands collection has not sorted yet. Need to sort by count
	 * 
	 * @author minhvv2
	 * @param brands
	 * @param brandId2Count
	 * @return
	 */
	public Collection<BrandBean> getTop20Brands(Collection<BrandBean> brands, Map<Integer, Integer> brandId2Count) {
		if (brands.size() > FILTER_MAX_MERCHANT) {
			Collection<BrandBean> top20BestBrand = new ArrayList<BrandBean>();
			brands = sortBrandCollectionByCount(brands, brandId2Count);
			Iterator<BrandBean> it = brands.iterator();
			int count = 0;
			while (count < FILTER_MAX_MERCHANT && it.hasNext()) {
				top20BestBrand.add(it.next());
				count++;
			}
			return top20BestBrand;
		} else {
			return brands;
		}
	}

	public Collection<BrandBean> reArrangeBrands(Collection<BrandBean> brands, Collection<String> requiredBrandIds,
			Map<Integer, Integer> brandId2Count) {
		if (brands.size() > FILTER_MAX_MERCHANT) {
			Collection<BrandBean> newBrands = new ArrayList<BrandBean>();
			Collection<BrandBean> requiredBrands = this.getRequiredBrandBean(brands, requiredBrandIds);
			if (requiredBrands.isEmpty()) {
				return getTop20Brands(brands, brandId2Count);
			}
			newBrands.addAll(requiredBrands);
			brands.removeAll(requiredBrands);
			brands = sortBrandCollectionByCount(brands, brandId2Count);
			int count = 0;
			Iterator<BrandBean> it = brands.iterator();
			while (it.hasNext() && count < FILTER_MAX_MERCHANT - requiredBrands.size()) {
				BrandBean brand = it.next();
				newBrands.add(brand);
				count++;
			}
			return newBrands;
		}
		return brands;
	}

	private Collection<BrandBean> sortBrandCollectionByCount(Collection<BrandBean> brands,
			Map<Integer, Integer> brandId2Count) {
		List<BrandBean> lstBeans = new ArrayList<BrandBean>(brands);
		lstBeans.sort(new Comparator<BrandBean>() {

			@Override
			public int compare(BrandBean brand1, BrandBean brand2) {
				if (brandId2Count.get(brand1.getId()) > brandId2Count.get(brand2.getId())) {
					return -1;
				} else if (brandId2Count.get(brand1.getId()) < brandId2Count.get(brand2.getId())) {
					return 1;
				}
				return 0;
			}
		});
		return lstBeans;
	}

	private Collection<BrandBean> getRequiredBrandBean(Collection<BrandBean> brands, Collection<String> requiredBrandIds) {
		Collection<BrandBean> requiredBeans = new ArrayList<BrandBean>();
		Iterator<BrandBean> it = brands.iterator();
		while (it.hasNext()) {
			BrandBean bb = it.next();
			if (requiredBrandIds.contains(String.valueOf(bb.getId()))) {
				requiredBeans.add(bb);
			}
		}

		return requiredBeans;
	}

}
