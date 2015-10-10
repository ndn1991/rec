package com.adr.bigdata.search.handler.db.sql.models;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.indexing.db.sql.beans.MerchantBean;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;
import com.adr.bigdata.search.handler.vo.FilterVO;

public class FilterModel extends BaseCommunicationModel {

	public FilterVO getFilter(int filteredCatId, Set<Integer> facetedCatIds, Set<Integer> brandIds, int catId,
			int merchantId) throws Exception {

		FilterVO filterVO = new FilterVO();

		filterVO.setCategories(getModelFactory().getModel(FacetedCategoryModel.class).getFacetCategories(facetedCatIds));

		filterVO.setBrands(getModelFactory().getModel(BrandModel.class).getBrands(brandIds));

		filterVO.setCategoryTree(getModelFactory().getModel(CategoryTreeModel.class).getCategoryTree(catId));

		filterVO.setMerchantBean(getModelFactory().getModel(MerchantModel.class).getMerchant(merchantId));

		return filterVO;
	}

	public Collection<BrandBean> getFacetedBrands(Set<Integer> facetedBrandIds) throws Exception {
		if (facetedBrandIds == null || facetedBrandIds.isEmpty()) {
			return null;
		}
		return getModelFactory().getModel(BrandModel.class).getBrands(facetedBrandIds);

	}

	public Collection<MerchantBean> getFacetedMerchants(Set<Integer> facetedMerchants) throws Exception {
		if (facetedMerchants == null || facetedMerchants.isEmpty()) {
			return null;
		}
		return getModelFactory().getModel(MerchantModel.class).getAllMerchants(facetedMerchants);

	}

	public Set<AttributeCategoryMappingBean> getAttByCategoryId(int categoryId) throws Exception {
		if (categoryId < 0) {
			return null;
		}
		return getModelFactory().getModel(AttributeModel.class).getAttributesByCat(categoryId);
	}

	public Map<Integer, AttributeCategoryMappingBean> getAttributesMapByCat(int catId) throws Exception {
		if (catId < 0) {
			return null;
		}
		return getModelFactory().getModel(AttributeModel.class).getAttributesMapByCat(catId);
	}

	public BrandBean getBrand(int brandId) {
		if (brandId < 0) {
			return null;// no need to gen model
		}
		return getModelFactory().getModel(BrandModel.class).getBrand(brandId);
	}

	public CategoryTreeVO getCatTree(int catId) throws Exception {
		if (catId < 0) {
			return null;
		}
		return getModelFactory().getModel(CategoryTreeModel.class).getCategoryTree(catId);
	}

	public Collection<CategoryBean> getFacetCategories(Set<Integer> facetedCatIds) throws Exception {
		if (facetedCatIds == null | facetedCatIds.isEmpty()) {
			return null;
		}
		return getModelFactory().getModel(FacetedCategoryModel.class).getFacetCategories(facetedCatIds);
	}

	public CategoryBean getCategoryBean(int catId) {
		return getModelFactory().getModel(FacetedCategoryModel.class).getCategoryBean(catId);
	}

	public MerchantBean getMerchant(int merchantId) throws Exception {
		if (merchantId < 0) {
			return null;
		}
		return getModelFactory().getModel(MerchantModel.class).getMerchant(merchantId);
	}

}
