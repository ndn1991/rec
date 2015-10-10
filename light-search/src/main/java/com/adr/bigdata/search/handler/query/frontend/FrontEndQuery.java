package com.adr.bigdata.search.handler.query.frontend;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.AttributeFilter;
import com.adr.bigdata.search.handler.query.common.Schema;
import com.adr.bigdata.search.handler.utils.ConvertStringToString;
import com.adr.bigdata.search.handler.utils.StringUtils;
import com.google.common.base.Strings;
//import org.apache.solr.client.solrj.

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class FrontEndQuery {

	public static FrontEndQuery createFrontendQuery(SolrQueryRequest req) {
		FrontEndQuery ret = new FrontEndQuery();
		SolrParams param = req.getParams();
		ret.setKeyword(param.get(Param.KEYWORD.toString()));
		ret.setFilter(param.get(Param.FILTER.toString()));
		ret.setCateogryId(param.get(Param.CAT_ID.toString()));
		ret.setIsGetBrand(param.get(Param.IS_GET_BRAND.toString()));
		ret.setManufacturerId(param.get(Param.MANUFACTURER_ID.toString()));
		ret.setMerchantIds(param.get(Param.MERCHANT_ID.toString()));
		ret.setPrices(param.get(Param.PRICE.toString()));
		ret.setStart(param.get(Param.START.toString()));
		ret.setRows(param.get(Param.ROWS.toString()));
		ret.setSortType(param.get(Param.ENUM.toString()));
		ret.setIsAscending(param.get(Param.IS_ASCENDING.toString()));
		ret.setIsPromotion(param.get(Param.IS_PROMOTION.toString()));
		ret.setFilteredCategoryId(param.get(Param.FILTERED_CATE_ID.toString()));
		ret.setCityId(param.get(Param.CITY_ID.toString()));
		ret.setIsGetMerchant(param.get(Param.IS_GET_MERCHANT.toString()));
		ret.setIsNew(param.get(Param.IS_NEW.toString()));
		ret.setDistrict(param.get(Param.DISTRICT.toString())); //getOrElse 0 as default value
		return ret;
	}

	private String keyword;
	private String isNew;
	private String cateogryId;
	private String filteredCategoryId;
	private int isGetBrand;
	private List<String> brandIds;
	private String manufacturerId;
	private Collection<String> prices;
	private List<String> merchantIds;
	private String start;
	private String rows;
	private String sortType;
	private String isAscending;
	private String isPromotion;
	private Set<AttributeFilter> attributeFilters;
	private boolean searchBySku;
	private String cityId;
	private int isGetMerchant;
	private String district;
	
	

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public boolean isSearchBySku() {
		return searchBySku;
	}

	public String getIsNew() {
		return isNew;
	}

	public void setIsNew(String isNew) {
		this.isNew = isNew;
	}

	public String getKeyword() {
		return keyword;
	}

	public Set<AttributeFilter> getAttributeFilters() {
		return attributeFilters;
	}

	public String getCateogryId() {
		return cateogryId;
	}

	public int getIsGetBrand() {
		return isGetBrand;
	}

	public int getIsGetMerchant() {
		return isGetMerchant;
	}

	public List<String> getBrandIds() {
		return brandIds;
	}

	public String getManufacturerId() {
		return manufacturerId;
	}

	public Collection<String> getPrices() {
		return prices;
	}

	public List<String> getMerchantIds() {
		return merchantIds;
	}

	public String getStart() {
		return start;
	}

	public String getRows() {
		return rows;
	}

	public String getSortType() {
		return sortType;
	}

	public String getIsAscending() {
		return isAscending;
	}

	public String getIsPromotion() {
		return isPromotion;
	}

	public String getFilteredCategoryId() {
		return filteredCategoryId;
	}

	public void setFilteredCategoryId(String filteredCategoryId) {
		this.filteredCategoryId = filteredCategoryId;
	}

	public void setIsGetBrand(String isGetBrand) {
		try {
			this.isGetBrand = Integer.parseInt(isGetBrand);
		} catch (Exception e) {
			this.isGetBrand = 0;
		}
	}

	public void setIsGetMerchant(String isGetMerchant) {
		try {
			this.isGetMerchant = Integer.parseInt(isGetMerchant);
		} catch (Exception e) {
			this.isGetMerchant = 0;
		}
	}

	public void setManufacturerId(String manufacturerId) {
		if (!Strings.isNullOrEmpty(manufacturerId)) {
			String[] brandIdSplitted = manufacturerId.split("--");
			this.brandIds = new ArrayList<>();
			for (String s : brandIdSplitted) {
				try {
					Integer.parseInt(s);
					this.brandIds.add(s);
				} catch (Exception e) {
				}
			}
		}
	}

	public void setPrices(String prices) {
		if (!Strings.isNullOrEmpty(prices)) {
			this.prices = new LinkedList<>();
			for (String p : prices.split("--")) {
				this.prices.add(p);
			}
		}
	}

	/**
	 * @quangvh if this case does not search via SKU, we need repair keyword
	 */
	public void setKeyword(String keyword) {
		if (Strings.isNullOrEmpty(keyword)) {
			this.keyword = "*:*";
		} else {
			this.keyword = ConvertStringToString.decodeSumaryToNormal(keyword);
			this.searchBySku = StringUtils.searchBySku(this.keyword);
			this.keyword = ConvertStringToString.decodeSumaryToNormal(keyword);
			this.keyword = this.searchBySku ? queryBySku(StringUtils
					.extractSku(this.keyword)) : StringUtils
					.nomalizationStringSearch(this.keyword);
		}
	}

	private String queryBySku(String sku) {
		return new StringBuilder().append(Schema.MERCHANT_PRODUCT_ITEM_SKU)
				.append(":").append(sku).toString();
	}

	public void setFilter(String filter) {
		if (!Strings.isNullOrEmpty(filter)) {
			this.attributeFilters = new HashSet<>();
			String[] attributeFiltersArr = filter.split("::");
			for (String attFilter : attributeFiltersArr) {
				String[] attValues = attFilter.split("--");
				if (attValues.length >= 2) {
					String attId = attValues[0].trim();
					AttributeFilter attributeFilter = new AttributeFilter(attId);
					for (int i = 1; i < attValues.length; i++) {
						attributeFilter.addFilter(attValues[i]);
					}

					this.attributeFilters.add(attributeFilter);
				}
			}
		}
	}

	public void setStart(String start) {
		if (Strings.isNullOrEmpty(start)) {
			this.start = "0";
		} else {
			this.start = start;
		}
	}

	public void setRows(String rows) {
		if (Strings.isNullOrEmpty(rows)) {
			this.rows = "24";
		} else {
			this.rows = rows;
		}
	}

	public void setSortType(String sortType) {
		this.sortType = sortType;
	}

	public void setIsAscending(String isAscending) {
		this.isAscending = isAscending;
	}

	public void setIsPromotion(String isPromotion) {
		if (Strings.isNullOrEmpty(isPromotion)) {
			this.isPromotion = "";
		}
		this.isPromotion = isPromotion;
	}

	public void setCateogryId(String cateogryId) {
		this.cateogryId = cateogryId;
	}

	public void setMerchantIds(String merchantId) {
		if (Strings.isNullOrEmpty(merchantId)) {
			this.merchantIds = Collections.emptyList();
			return;
		}
		String[] splittedMerchants = merchantId.split("--");
		this.merchantIds = Arrays.asList(splittedMerchants);
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		this.cityId = cityId;
	}

	enum Param {
		KEYWORD("keyword"), FILTER("filter"), CAT_ID("catid"), ENUM("enum"), IS_GET_BRAND(
				"isgetbrand"), MERCHANT_ID("merchantid"), START("offset"), ROWS(
				"limit"), IS_ASCENDING("isasc"), IS_PROMOTION("ispromotion"), MANUFACTURER_ID(
				"manufactuerid"), PRICE("price"), FILTERED_CATE_ID(
				"catfilterid"), CITY_ID("cityid"), IS_GET_MERCHANT(
				"isgetmerchant"), IS_NEW("isnew"), DISTRICT("districtid");

		private String param;

		private Param(String param) {
			this.param = param;
		}

		@Override
		public String toString() {
			return this.param;
		}

	}
}
