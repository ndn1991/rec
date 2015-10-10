/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.adr.bigdata.search.handler.db.sql.models.BrandModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.query.NamedListHelper;
import com.adr.bigdata.search.handler.response.daos.GetFilterReturnField;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
public class BrandWriter extends WriterStrategy implements DescriptableWriter, Loggable {
	private BrandSorter brandSorter;

	public BrandWriter() {
		this.brandSorter = new BrandSorter();
		this.model = ModelFactory.newInstance().getModel(BrandModel.class);
	}

	@Override
	public void writeComponent(SolrQueryResponse response, NamedList facetCount, Object... data) {
		assert data.length >= 2;
		SolrCache cache = (SolrCache) data[0];
		List<Integer> brandIdsFilter = (List) data[1];
		List<BrandBean> brands = null;
		Map<Integer, Integer> brandId2Count = extractBrandId2Count(facetCount);
		try {
			brands = getBrands(brandId2Count.keySet(), cache);
		} catch (Exception e) {
			//do nothing
		}

		if (brands != null) {
			if (brandIdsFilter != null && !brandIdsFilter.isEmpty()) {
				Collection<BrandBean> reArrangedBrand = brandSorter.reArrangeBrands(brands, brandIdsFilter,
						brandId2Count);
				response.add(GetFilterReturnField.LIST_CAT_MAN,
						NamedListHelper.convertList(reArrangedBrand, brandId2Count));
			} else {
				Collection<BrandBean> top20Brands = brandSorter.getTop20Brands(brands, brandId2Count);
				response.add(GetFilterReturnField.LIST_CAT_MAN, NamedListHelper.convertList(top20Brands, brandId2Count));
			}
		}

	}

	@Override
	public void writeInfor(SolrQueryResponse response, int brandId, Object... data) {
		assert data.length >= 1;
		SolrCache cache = (SolrCache) data[0];
		String key = "brand_" + brandId;
		Object brandInfor = cache.get(key);
		if (brandInfor == null) {
			try {
				brandInfor = ((BrandModel) model).getBrand(brandId);
				cache.put(key, brandInfor);
			} catch (Exception e) {
				// do nothing
			}
		}
		response.add(GetFilterReturnField.BRAND_NAME, ((BrandBean) brandInfor).getName());
		response.add(GetFilterReturnField.BRAND_IMAGE, ((BrandBean) brandInfor).getImage());
		response.add(GetFilterReturnField.BRAND_ID, ((BrandBean) brandInfor).getId());
		response.add(GetFilterReturnField.BRAND_SHORT_INFO, ((BrandBean) brandInfor).getDescription());
	}

	private List<BrandBean> getBrands(Set<Integer> facetedBrandIds, SolrCache cache) throws Exception {
		BrandModel brandModel = (BrandModel) model;
		List<BrandBean> result = new ArrayList<BrandBean>();
		for (int brandId : facetedBrandIds) {
			String key = "brand_" + brandId;
			Object cacheBrand = cache.get(key);
			if (cacheBrand == null) {
				cacheBrand = brandModel.getBrand(brandId);
				cache.put(key, cacheBrand);
			}
			result.add((BrandBean) cacheBrand);
		}
		return result;

	}

	private Map<Integer, Integer> extractBrandId2Count(NamedList facetCounts) {
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

	class BrandSorter {
		private static final int FILTER_MAX_MERCHANT = 20;

		/**
		 * the brands collection has not sorted yet. Need to sort by count
		 * 
		 * @author minhvv2
		 * @param brands
		 * @param brandId2Count
		 * @return
		 */
		Collection<BrandBean> getTop20Brands(Collection<BrandBean> brands, Map<Integer, Integer> brandId2Count) {
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

		Collection<BrandBean> reArrangeBrands(Collection<BrandBean> brands, Collection<Integer> requiredBrandIds,
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

		Collection<BrandBean> sortBrandCollectionByCount(Collection<BrandBean> brands,
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

		Collection<BrandBean> getRequiredBrandBean(Collection<BrandBean> brands, Collection<Integer> requiredBrandIds) {
			Collection<BrandBean> requiredBeans = new ArrayList<BrandBean>();
			Iterator<BrandBean> it = brands.iterator();
			while (it.hasNext()) {
				BrandBean bb = it.next();
				if (requiredBrandIds.contains(bb.getId())) {
					requiredBeans.add(bb);
				}
			}
			return requiredBeans;
		}
	}

}
