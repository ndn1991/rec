package com.adr.bigdata.search.handler.query.frontend;

import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.CAT_ID;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.CITY_ID;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.FILTER;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.FILTERED_CATE_ID;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.IS_GET_BRAND;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.IS_GET_MERCHANT;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.IS_PROMOTION;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.IS_NEW;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.KEYWORD;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.MANUFACTURER_ID;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.MERCHANT_ID;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.PRICE;
import static com.adr.bigdata.search.handler.query.frontend.GetFilterQuery.Params.DISTRICT_ID;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.AttributeFilter;
import com.adr.bigdata.search.handler.query.common.Schema;
import com.adr.bigdata.search.handler.utils.ConvertStringToString;
import com.adr.bigdata.search.handler.utils.StringUtils;
import com.google.common.base.Strings;
import com.nhb.common.Loggable;

public class GetFilterQuery implements Loggable {
	private String keyword;
	private String cateogryId;
	private String filteredCategoryId;
	private int isGetBrand;
	private int isGetMerchant;
	private Collection<String> brandIds;
	private String manufacturerId;
	private List<String> prices;
	private String merchantId;
	private String isPromotion;
	private String isNew;
	private Set<AttributeFilter> attributeFilters;
	private boolean searchBySku;
	private String cityId;
	private String districtId;

	public String getDistrictId() {
		return districtId;
	}

	public void setDistrictId(String districtId) {
		if (Strings.isNullOrEmpty(districtId)) {
			this.districtId = "";
		} else {
			this.districtId = districtId.trim();
		}
	}

	private Set<Integer> lstMerchantFilters;

	public boolean isGetFeatured() {
		return !Strings.isNullOrEmpty(isPromotion) || !Strings.isNullOrEmpty(isNew);
	}

	public boolean isGetMerchant() {
		return isGetMerchant == 1;
	}

	public String getIsNew() {
		return isNew;
	}

	public void setIsNew(String isNew) {
		this.isNew = isNew;
	}

	public void setIsGetMerchant(String isGetMerchant) {
		try {
			this.isGetMerchant = Integer.valueOf(isGetMerchant);
		} catch (Exception e) {
			this.isGetMerchant = 0;
		}

	}

	public Set<Integer> getLstMerchantFilters() {
		return lstMerchantFilters;
	}

	public String getCityId() {
		return cityId;
	}

	public void setCityId(String cityId) {
		if (Strings.isNullOrEmpty(cityId)) {
			this.cityId = "";
		} else {
			this.cityId = cityId.trim();
		}
	}

	public boolean isSearchByCat() {
		return !Strings.isNullOrEmpty(cateogryId);
	}

	public boolean isSearchByCatFilterId() {
		return !Strings.isNullOrEmpty(filteredCategoryId);
	}

	public boolean isSearchBySku() {
		return searchBySku;
	}

	public GetFilterQuery(SolrQueryRequest req) {
		SolrParams param = req.getParams();
		setKeyword(param.get(KEYWORD));
		setFilter(param.get(FILTER));
		setCityId(param.get(CITY_ID));
		setDistrictId(param.get(DISTRICT_ID));
		setCateogryId(param.get(CAT_ID));
		setIsGetBrand(param.get(IS_GET_BRAND));
		setIsGetMerchant(param.get(IS_GET_MERCHANT));
		setManufacturerId(param.get(MANUFACTURER_ID));
		setMerchantId(param.get(MERCHANT_ID));
		setPrices(param.get(PRICE));
		setIsNew(param.get(IS_NEW));
		setIsPromotion(param.get(IS_PROMOTION));
		setFilteredCategoryId(param.get(FILTERED_CATE_ID));
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

	public Collection<String> getBrandIds() {
		return brandIds;
	}

	public String getManufacturerId() {
		return manufacturerId;
	}

	public List<String> getPrices() {
		return prices;
	}

	public String getMerchantId() {
		return merchantId;
	}

	public String getIsPromotion() {
		return this.isPromotion;
	}

	public String getFilteredCategoryId() {
		return filteredCategoryId;
	}

	public void setFilteredCategoryId(String filteredCategoryId) {
		if (Strings.isNullOrEmpty(filteredCategoryId)) {
			this.filteredCategoryId = "";
		} else {
			this.filteredCategoryId = filteredCategoryId.trim();
		}
	}

	public void setIsGetBrand(String isGetBrand) {
		try {
			this.isGetBrand = Integer.parseInt(isGetBrand);
		} catch (Exception e) {
			this.isGetBrand = 0;
		}
	}

	public void setManufacturerId(String manufacturerId) {
		if (!Strings.isNullOrEmpty(manufacturerId)) {
			String[] brandIds = manufacturerId.trim().split("--");
			this.brandIds = new ArrayList<String>();
			for (String brand : brandIds) {
				if (!Strings.isNullOrEmpty(brand)) {
					this.brandIds.add(brand);
				}
			}
		}
	}

	public void setPrices(String prices) {
		if (!Strings.isNullOrEmpty(prices)) {
			this.prices = Arrays.asList(prices.trim().split("--"));
		}
	}

	public void setKeyword(String keyword) {
		if (Strings.isNullOrEmpty(keyword)) {
			this.keyword = "*:*";
		} else {
			this.keyword = ConvertStringToString.decodeSumaryToNormal(keyword.trim());
			this.searchBySku = StringUtils.searchBySku(this.keyword);
			this.keyword = this.searchBySku ? queryBySku(StringUtils.extractSku(this.keyword)) : StringUtils
					.nomalizationStringSearch(this.keyword);
		}
	}

	public void setFilter(String filter) {
		if (!Strings.isNullOrEmpty(filter)) {
			this.attributeFilters = new HashSet<>();
			String[] attributeFiltersArr = filter.trim().split("::");
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

	public void setIsPromotion(String isPromotion) {
		if (Strings.isNullOrEmpty(isPromotion)) {
			this.isPromotion = "0";
		} else {
			this.isPromotion = isPromotion.trim();
		}
	}

	public void setCateogryId(String cateogryId) {
		if (Strings.isNullOrEmpty(cateogryId)) {
			this.cateogryId = "";
		} else {
			this.cateogryId = cateogryId.trim();
		}
	}

	public void setMerchantId(String merchantId) {
		if (Strings.isNullOrEmpty(merchantId)) {
			this.merchantId = "";
		} else {
			this.merchantId = merchantId;
			String[] merchantIds = merchantId.trim().split("--");
			this.lstMerchantFilters = new HashSet<Integer>();
			for (String merchant : merchantIds) {
				if (!Strings.isNullOrEmpty(merchant)) {
					this.lstMerchantFilters.add(Integer.valueOf(merchant));
				}
			}

		}
	}


	private String queryBySku(String sku) {
		return new StringBuilder().append(Schema.MERCHANT_PRODUCT_ITEM_SKU).append(":").append(sku).toString();
	}

	static class Params {
		final static String CITY_ID = "cityid";
		final static String KEYWORD = "keyword";
		final static String FILTER = "filter";
		final static String CAT_ID = "catid";
		final static String ENUM = "enum";
		final static String IS_GET_BRAND = "isgetbrand";
		final static String IS_GET_MERCHANT = "isgetmerchant";
		final static String MERCHANT_ID = "merchantid";
		final static String IS_PROMOTION = "ispromotion";
		final static String MANUFACTURER_ID = "manufactuerid";
		final static String PRICE = "price";
		final static String FILTERED_CATE_ID = "catfilterid";
		final static String IS_NEW = "isnew";
		final static String DISTRICT_ID = "districtid";
	}

}