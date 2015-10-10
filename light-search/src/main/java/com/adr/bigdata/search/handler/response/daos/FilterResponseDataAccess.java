package com.adr.bigdata.search.handler.response.daos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.indexing.db.sql.beans.MerchantBean;
import com.adr.bigdata.search.handler.db.sql.models.AttributeModel;
import com.adr.bigdata.search.handler.db.sql.models.FilterModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.facetextractor.FacetResultExtractor;
import com.adr.bigdata.search.handler.query.NamedListHelper;
import com.adr.bigdata.search.handler.query.frontend.GetFilterQuery;
import com.adr.bigdata.search.handler.responsestrategy.bean.FilterModelQueryBean;
import com.adr.bigdata.search.handler.utils.CommonConstant;
import com.adr.bigdata.search.handler.utils.DoubleUtils;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;
import com.google.common.base.Strings;
import com.nhb.common.Loggable;

public class FilterResponseDataAccess implements Loggable {
	private BrandCollectionExtractor brandSorter = new BrandCollectionExtractor();

	private final FilterModel filterModel;
	private HazelCastWorkSubmittor submittor;

	public FilterResponseDataAccess(FilterModel filterModel, ExecutorService pool) {
		this.filterModel = filterModel;
		this.submittor = new HazelCastWorkSubmittor(filterModel, pool);
	}

	public FilterModelQueryBean getFilterModelBean(GetFilterQuery filterQuery, FacetResultExtractor extractor) {
		FilterModelQueryBean result = new FilterModelQueryBean();
		try {
			if (!Strings.isNullOrEmpty(filterQuery.getCateogryId())) {
				result.setCategoryId(Integer.valueOf(filterQuery.getCateogryId()));
			} else {
				result.setCategoryId(-1);
			}
		} catch (Exception ex) {
			result.setCategoryId(-1);
		}
		try {
			if (!Strings.isNullOrEmpty(filterQuery.getFilteredCategoryId())) {
				result.setFilteredCategoryId(Integer.valueOf(filterQuery.getFilteredCategoryId()));
			} else {
				result.setFilteredCategoryId(-1);
			}
		} catch (Exception ex) {
			result.setFilteredCategoryId(-1);
		}
		try {
			if (!Strings.isNullOrEmpty(filterQuery.getMerchantId())) {
				result.setMerchantId(Integer.valueOf(filterQuery.getMerchantId()));
			} else {
				result.setMerchantId(-1);
			}
		} catch (Exception ex) {
			result.setMerchantId(-1);
		}
		result.setFacetedBrandIds(extractor.generateFacetedBrands());
		result.setFacetedCategoryIds(extractor.generateFacetedCategoryIds());
		result.setFacetedMerchantIds(extractor.generateFacetedMerchants());
		return result;
	}

	/**
	 * new version of writeWithPrebuiltTree function: cache the whole bean to
	 * solr cache
	 * 
	 * @param filterQuery
	 * @param rsp
	 * @param req
	 * @param facetCounts
	 * @param cachedTree
	 * @throws Exception
	 */
	public void writeCateBrowsingQuery(GetFilterQuery filterQuery, SolrQueryResponse rsp, SolrQueryRequest req,
			NamedList facetCounts, CategoryTreeVO cachedTree) throws Exception {
		FacetResultExtractor extractor = new FacetResultExtractor(facetCounts);
		FilterModelQueryBean modelQuery = getFilterModelBean(filterQuery, extractor);
		SolrCache cache = req.getSearcher().getCache(CommonConstant.CACHE_NAME);
		Collection<BrandBean> brands = getBrands(modelQuery.getFacetedBrandIds().keySet(), cache);
		if (brands != null) {
			if (filterQuery.getBrandIds() != null && !filterQuery.getBrandIds().isEmpty()) {
				Collection<BrandBean> reArrangedBrand = brandSorter.reArrangeBrands(brands, filterQuery.getBrandIds(),
						modelQuery.getFacetedBrandIds());
				rsp.add(GetFilterReturnField.LIST_CAT_MAN,
						NamedListHelper.convertList(reArrangedBrand, modelQuery.getFacetedBrandIds()));
			} else {
				Collection<BrandBean> top20Brands = brandSorter.getTop20Brands(brands, modelQuery.getFacetedBrandIds());
				rsp.add(GetFilterReturnField.LIST_CAT_MAN,
						NamedListHelper.convertList(top20Brands, modelQuery.getFacetedBrandIds()));
			}
		}

		Collection<MerchantBean> merchantBeans = getMerchants(modelQuery.getFacetedMerchantIds().keySet(), cache);

		if (cachedTree != null) {
			rsp.add(GetFilterReturnField.CATEGORY_TREE, cachedTree.toNamedList());
		} else {
			rsp.add(GetFilterReturnField.CATEGORY_TREE, null);
		}

		rsp.add(GetFilterReturnField.LIST_FEATURED, extractor.createListFeatured());

		if (!Strings.isNullOrEmpty(filterQuery.getFilteredCategoryId())) {
			Collection<CategoryBean> categories = null;

			try {
				categories = filterModel.getFacetCategories(modelQuery.getFacetedCategoryIds().keySet());
			} catch (Exception e) {
				getLogger().error("error getting categories....{}", e.getMessage());
			}
			rsp.add(GetFilterReturnField.LIST_CATEGORY,
					NamedListHelper.convertList(categories, modelQuery.getFacetedCategoryIds()));
		}

		if (merchantBeans != null) {
			rsp.add(GetFilterReturnField.LIST_MERCHANT,
					NamedListHelper.convertList(merchantBeans, modelQuery.getFacetedMerchantIds()));
		}

		try {
			writeAttributeFacetResult(filterQuery, rsp, facetCounts, cache);
		} catch (Exception e) {
			getLogger().error("error getting writeAttributeFacetResult....{}", e.getMessage());
		}

	}

	//TODO: use solr cache here
	private Collection<BrandBean> getBrands(Set<Integer> facetedBrandIds, SolrCache cache) throws Exception {
		//		Object cachedBrands = cache.get("brandSet_" + facetedBrandIds.toString());
		//		if (cachedBrands == null) {
		//			cachedBrands = filterModel.getFacetedBrands(facetedBrandIds);
		//			cache.put("brandSet_" + facetedBrandIds.toString(), cachedBrands);
		//		}
		//		return (Collection<BrandBean>) cachedBrands;
		return filterModel.getFacetedBrands(facetedBrandIds);

	}

	private Collection<MerchantBean> getMerchants(Set<Integer> facetedMerchantIds, SolrCache cache) throws Exception {
		//		Object cachedMerchants = cache.get("mcSet_" + facetedMerchantIds.toString());
		//		if (cachedMerchants == null) {
		//			cachedMerchants = filterModel.getFacetedMerchants(facetedMerchantIds);
		//			cache.put("mcSet_" + facetedMerchantIds.toString(), cachedMerchants);
		//		}
		//		return (Collection<MerchantBean>) cachedMerchants;

		return filterModel.getFacetedMerchants(facetedMerchantIds);

	}

	private Collection<CategoryBean> getCategories(Set<Integer> facetedCatIds, SolrCache cache) throws Exception {
		//		Object cachedCategories = cache.get("catSet_" + facetedCatIds.toString());
		//		if (cachedCategories == null) {
		//			cachedCategories = filterModel.getFacetCategories(facetedCatIds);
		//			cache.put("catSet_" + facetedCatIds.toString(), cachedCategories);
		//		}
		//		return (Collection<CategoryBean>) cachedCategories;
		return filterModel.getFacetCategories(facetedCatIds);

	}

	@Deprecated
	//use function writeCateBrowsingQuery instead
	public void writeWithPrebuiltTree(GetFilterQuery filterQuery, SolrQueryResponse rsp, SolrQueryRequest req,
			NamedList facetCounts, CategoryTreeVO cachedTree) {
		FacetResultExtractor extractor = new FacetResultExtractor(facetCounts);
		FilterModelQueryBean modelQuery = getFilterModelBean(filterQuery, extractor);
		Collection<BrandBean> brands = null;
		Collection<MerchantBean> merchantBeans = null;
		SolrCache cache = req.getSearcher().getCache("nativecache");

		final Set<Integer> facetedBrandIds = modelQuery.getFacetedBrandIds().keySet();
		Future<Collection<BrandBean>> futureBrands = submittor.submitHazelJob(modelQuery, "brand");

		final Set<Integer> facetedMerchantIds = modelQuery.getFacetedMerchantIds().keySet();
		Future<Collection<MerchantBean>> futureMerchants = submittor.submitHazelJob(modelQuery, "merchant");

		if (cachedTree != null) {
			rsp.add(GetFilterReturnField.CATEGORY_TREE, cachedTree.toNamedList());
		} else {
			rsp.add(GetFilterReturnField.CATEGORY_TREE, null);
		}

		rsp.add(GetFilterReturnField.LIST_FEATURED, extractor.createListFeatured());

		if (!Strings.isNullOrEmpty(filterQuery.getFilteredCategoryId())) {
			Collection<CategoryBean> categories = null;

			try {
				categories = filterModel.getFacetCategories(modelQuery.getFacetedCategoryIds().keySet());
			} catch (Exception e) {
				getLogger().error("error getting categories....{}", e.getMessage());
			}
			rsp.add(GetFilterReturnField.LIST_CATEGORY,
					NamedListHelper.convertList(categories, modelQuery.getFacetedCategoryIds()));
		}

		try {
			brands = futureBrands.get();
		} catch (InterruptedException | ExecutionException ex) {
			getLogger().error("error while getting faceted brands...{}", ex.getMessage());
		}
		if (brands != null) {
			if (filterQuery.getBrandIds() != null && !filterQuery.getBrandIds().isEmpty()) {
				Collection<BrandBean> reArrangedBrand = brandSorter.reArrangeBrands(brands, filterQuery.getBrandIds(),
						modelQuery.getFacetedBrandIds());
				rsp.add(GetFilterReturnField.LIST_CAT_MAN,
						NamedListHelper.convertList(reArrangedBrand, modelQuery.getFacetedBrandIds()));
			} else {
				Collection<BrandBean> top20Brands = brandSorter.getTop20Brands(brands, modelQuery.getFacetedBrandIds());
				rsp.add(GetFilterReturnField.LIST_CAT_MAN,
						NamedListHelper.convertList(top20Brands, modelQuery.getFacetedBrandIds()));
			}
		}

		try {
			merchantBeans = futureMerchants.get();
		} catch (InterruptedException | ExecutionException ex) {
			getLogger().error("error while getting faceted merchants...{}", ex.getMessage());
		}

		if (merchantBeans != null) {
			rsp.add(GetFilterReturnField.LIST_MERCHANT,
					NamedListHelper.convertList(merchantBeans, modelQuery.getFacetedMerchantIds()));
		}

		try {
			writeAttributeFacetResult(filterQuery, rsp, facetCounts, cache);
		} catch (Exception e) {
			getLogger().error("error getting writeAttributeFacetResult....{}", e.getMessage());
		}

	}

	public void writeToResponse(GetFilterQuery filterQuery, SolrQueryResponse rsp, SolrQueryRequest req,
			NamedList facetCounts) throws Exception {
		FacetResultExtractor extractor = new FacetResultExtractor(facetCounts);
		FilterModelQueryBean modelQuery = getFilterModelBean(filterQuery, extractor);
		SolrCache cache = req.getSearcher().getCache(CommonConstant.CACHE_NAME);

		Collection<BrandBean> brands = getBrands(modelQuery.getFacetedBrandIds().keySet(), cache);
		Collection<CategoryBean> categories = getCategories(modelQuery.getFacetedCategoryIds().keySet(), cache);
		Collection<MerchantBean> merchantBeans = getMerchants(modelQuery.getFacetedMerchantIds().keySet(), cache);

		if (filterQuery.getIsGetBrand() != 0) {
			int brandId = Integer.valueOf(filterQuery.getBrandIds().iterator().next());

			Object brand = cache.get("brand_" + brandId);
			if (brand == null) {
				brand = filterModel.getBrand(brandId);
				cache.put("brand_" + brandId, brand);
			}
			rsp.add(GetFilterReturnField.BRAND_NAME, ((BrandBean) brand).getName());
			rsp.add(GetFilterReturnField.BRAND_IMAGE, ((BrandBean) brand).getImage());
			rsp.add(GetFilterReturnField.BRAND_ID, ((BrandBean) brand).getId());
			rsp.add(GetFilterReturnField.BRAND_SHORT_INFO, ((BrandBean) brand).getDescription());
		}

		rsp.add(GetFilterReturnField.LIST_FEATURED, extractor.createListFeatured());
		rsp.add(GetFilterReturnField.LIST_CATEGORY,
				NamedListHelper.convertList(categories, modelQuery.getFacetedCategoryIds()));

		if (brands != null) {
			if (filterQuery.getBrandIds() != null && !filterQuery.getBrandIds().isEmpty()
					& filterQuery.getIsGetBrand() != 1) {
				Collection<BrandBean> reArrangedBrand = brandSorter.reArrangeBrands(brands, filterQuery.getBrandIds(),
						modelQuery.getFacetedBrandIds());
				rsp.add(GetFilterReturnField.LIST_CAT_MAN,
						NamedListHelper.convertList(reArrangedBrand, modelQuery.getFacetedBrandIds()));
			} else {
				Collection<BrandBean> top20Brands = brandSorter.getTop20Brands(brands, modelQuery.getFacetedBrandIds());
				rsp.add(GetFilterReturnField.LIST_CAT_MAN,
						NamedListHelper.convertList(top20Brands, modelQuery.getFacetedBrandIds()));
			}
		}

		if (!filterQuery.isGetMerchant()) {
			rsp.add(GetFilterReturnField.LIST_MERCHANT,
					NamedListHelper.convertList(merchantBeans, modelQuery.getFacetedMerchantIds()));
		}

		writeAttributeFacetResult(filterQuery, rsp, facetCounts, cache);

		if (filterQuery.isGetMerchant()) {
			Object merchantInfor = cache.get("merchant_" + filterQuery.getMerchantId());
			if (merchantInfor == null) {
				merchantInfor = filterModel.getMerchant(modelQuery.getMerchantId());
				cache.put("merchant_" + filterQuery.getMerchantId(), merchantInfor);
			}
			rsp.add(GetFilterReturnField.MERCHANT_NAME, ((MerchantBean) merchantInfor).getName());
			rsp.add(GetFilterReturnField.MERCHANT_SHORT_INFO, ((MerchantBean) merchantInfor).getInfo());
			rsp.add(GetFilterReturnField.MERCHANT_IMAGE, ((MerchantBean) merchantInfor).getImage());
			rsp.add(GetFilterReturnField.MERCHANT_ID, ((MerchantBean) merchantInfor).getId());
		}

	}

	@SuppressWarnings("unchecked")
	private void writeAttributeFacetResult(GetFilterQuery filterQuery, SolrQueryResponse rsp, NamedList facetCounts,
			SolrCache cache) throws Exception {

		Map<Integer, AttributeCategoryMappingBean> cat2Attribute = (Map<Integer, AttributeCategoryMappingBean>) rsp
				.getValues().get("attributeCached");
		rsp.getValues().remove("attributeCached");

		NamedList facetFields = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
		if (facetFields == null) {
			return;
		}
		int catId = -1;
		try {
			catId = Integer.valueOf(filterQuery.getCateogryId());
		} catch (Exception e) {
			catId = -1;
		}

		if (cat2Attribute == null) {
			return;
		}

		List<NamedList> lstCatAtt = new ArrayList<NamedList>();
		AttributeModel attModel = ModelFactory.newInstance().getModel(AttributeModel.class);

		for (int i = 0; i < facetFields.size(); i++) {
			String name = facetFields.getName(i);
			if (name.contains("attr")) {

				NamedList attFacet = (NamedList) facetFields.getVal(i);
				if (attFacet.size() > 0) {
					Integer attributeId = SolrResponseHelper.convertAttributeFieldName2AttId(name);
					if (!cat2Attribute.containsKey(attributeId)) {
						continue;
					}
					AttributeCategoryMappingBean mapper = cat2Attribute.get(attributeId);

					NamedList bean = new SimpleOrderedMap();
					bean.add("attId", attributeId);
					bean.add("attName", mapper.getAttributeName());
					bean.add("unitName", mapper.getUnitName());
					bean.add("typeValue", mapper.getAttributeType());
					List<NamedList> listValueBean = new ArrayList<NamedList>();
					Map<String, String> value2joinedStr = new HashMap<String, String>();
					for (int j = 0; j < attFacet.size(); j++) {
						String value = attFacet.getName(j);
						NamedList valueBean = new SimpleOrderedMap();
						valueBean.add("value", value);
						valueBean.add("numFound", attFacet.getVal(j));
						if (mapper.getAttributeType() == 4) {
							Double doubleVal = null;
							try {
								doubleVal = Double.valueOf(value);
							} catch (NumberFormatException ex) {
								doubleVal = null;
							}
							if (doubleVal != null) {
								value2joinedStr.put(value,
										attributeId.toString() + "_" + DoubleUtils.formatDouble(doubleVal));
							}
						}
						listValueBean.add(valueBean);
					}

					if (value2joinedStr.size() > 0) {
						Map<String, String> transformed2DisplayValue = null;
						transformed2DisplayValue = (Map) cache.get(value2joinedStr);
						if (transformed2DisplayValue == null) {
							transformed2DisplayValue = attModel.getDisplayAttribute(value2joinedStr);
							cache.put(value2joinedStr, transformed2DisplayValue);
						}
						for (NamedList valBean : listValueBean) {
							String normalVal = (String) valBean.get("value");
							String displayVal = transformed2DisplayValue.get(normalVal);
							if (Strings.isNullOrEmpty(displayVal)) {
								displayVal = DoubleUtils.transform(Double.valueOf(normalVal), mapper.getUnitName());
							}
							valBean.add("displayValue", displayVal);
						}
					}

					bean.add("lvalue", listValueBean);
					lstCatAtt.add(bean);
				}
			}
		}

		rsp.add("listCatAttFilter", lstCatAtt);

	}

}
