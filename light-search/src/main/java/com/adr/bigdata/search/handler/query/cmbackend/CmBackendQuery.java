/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.query.cmbackend;

import com.adr.bigdata.search.handler.utils.StringUtils;
import com.google.api.client.repackaged.com.google.common.base.Objects;
import com.google.api.client.repackaged.com.google.common.base.Strings;
import com.google.common.base.Joiner;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

/**
 *
 * @author Tong Hoang Anh
 */
public final class CmBackendQuery {

	private String[] merchantIds;
	private String[] categoryIds;
	private String[] brandIds;
	private String[] warehouseIds;
	private String[] warehouseProductItemMappingId;
	private String[] productItemIds;
	private String visible;
	private String[] merchantProductItemStatuses;
	private String[] productItemStatuses;
	private String[] productItemTypes;
	private String inStock;

	private String keyword = Default.KEYWORD;
	private String dateFromTo;
	private String rows = Default.ROWS;
	private String start = Default.START;
	private String sort = Params.SORT;
	private String safetyStock;
	private String isoriginal;
	
	private String approved;

	public CmBackendQuery() {

	}

	public CmBackendQuery(SolrQueryRequest req) {
		SolrParams param = req.getParams();
		setKeyword(param.get(Params.KEYWORD));
		this.start = param.get(Params.START);
		this.rows = param.get(Params.ROWS);
		this.sort = param.get(Params.SORT);
		this.dateFromTo = param.get(Params.DATE_FROM_TO);
		this.categoryIds = param.getParams(Params.CATEGORY_ID);
		this.warehouseIds = param.getParams(Params.WAREHOUSE_ID);
		this.warehouseProductItemMappingId = param
				.getParams(Params.WAREHOUSE_PRODUCT_ITEM_MAPPING_ID);
		this.merchantIds = param.getParams(Params.MERCHANT_ID);
		this.brandIds = param.getParams(Params.BRAND_ID);
		this.productItemIds = param.getParams(Params.PRODUCT_ITEM_ID);
		this.visible = param.get(Params.VISIBLE);
		setProductItemStatuses(param.getParams(Params.PRODUCT_ITEM_STATUS));
		setMerchantProductItemStatuses(param
				.getParams(Params.MERCHANT_PRODUCT_ITEM_STATUS));
		this.productItemTypes = param.getParams(Params.PRODUCT_ITEM_TYPE);
		setInStock(param.get(Params.IN_STOCK));
		setApproved(param.get(Params.APPROVED));
		this.safetyStock = param.get(Params.SAFETY_STOCK);
		this.isoriginal = param.get(Params.IS_ORIGINAL);
	}
	
	
	public String getIsOriginal(){
		return this.isoriginal;
	}
	
	public String getSafetyStock(){
		return this.safetyStock;
	}

	public String getApproved() {
		return approved;
	}

	public void setApproved(String approved) {
		this.approved = approved;
	}

	public String[] getMerchantProductItemStatuses() {
		return merchantProductItemStatuses;
	}

	public void setMerchantProductItemStatuses(
			String[] merchantProductItemStatuses) {
		if (merchantProductItemStatuses != null
				&& merchantProductItemStatuses.length != 0) {
			this.merchantProductItemStatuses = merchantProductItemStatuses;
			for (int i = 0; i < merchantProductItemStatuses.length; i++) {
				if (merchantProductItemStatuses[i].trim().equals("0")) {
					this.merchantProductItemStatuses = new String[] { "[0 TO *]" };
					return;
				}
				if (merchantProductItemStatuses[i].startsWith("-")) {
					this.merchantProductItemStatuses[i] = StringUtils
							.formatNegative(merchantProductItemStatuses[i]);
				}
			}
		}
	}

	public String[] getProductItemStatuses() {
		return productItemStatuses;
	}

	public void setProductItemStatuses(String[] productItemStatuses) {
		if (productItemStatuses != null && productItemStatuses.length != 0) {
			this.productItemStatuses = productItemStatuses;
			for (int i = 0; i < productItemStatuses.length; i++) {
				if (productItemStatuses[i].trim().equals("0")) {
					this.productItemStatuses = new String[] { "[0 TO *]" };
					return;
				}
				if (productItemStatuses[i].startsWith("-")) {
					this.productItemStatuses[i] = StringUtils
							.formatNegative(productItemStatuses[i]);
				}
			}
		}
	}

	public void setKeyword(String keyword) {
		if (Strings.isNullOrEmpty(keyword)) {
			this.keyword = Default.KEYWORD;
		} else {
			this.keyword = keyword;
		}
	}


	public String[] getCategoryIds() {
		return categoryIds;
	}

	public String[] getWarehouseIds() {
		return warehouseIds;
	}

	public String[] getBrandIds() {
		return brandIds;
	}

	public String[] getWarehouseProductItemMappingId() {
		return warehouseProductItemMappingId;
	}

	public String[] getMerchantIds() {
		return merchantIds;
	}

	public String[] getProductItemIds() {
		return productItemIds;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getDateFromTo() {
		return dateFromTo;
	}

	public String getRows() {
		return rows;
	}

	public String getStart() {
		return start;
	}

	public String getSort() {
		return sort;
	}

	public String getVisible() {
		return visible;
	}

	public String[] getProductItemTypes() {
		return productItemTypes;
	}

	public void setProductItemTypes(String[] productItemTypes) {
		this.productItemTypes = productItemTypes;
	}

	public String getInStock() {
		return inStock;
	}

	public void setInStock(String inStock) {
		if("true".equalsIgnoreCase(inStock)){
			this.inStock = InStock.TRUE;
		}else if("false".equalsIgnoreCase(inStock)){
			this.inStock = InStock.FALSE;
		}
	}

	private String arrayToString(String[] arr) {
		if (arr == null) {
			return null;
		}
		return Joiner.on(",").skipNulls().join(arr);
	}

	@Override
	public String toString() {
		return Objects
				.toStringHelper(getClass())
				.add("keyword", keyword)
				.add("start", start)
				.add("rows", rows)
				.add("sort", sort)
				.add("merchantIds", arrayToString(merchantIds))
				.add("categoryIds", arrayToString(categoryIds))
				.add("brandIds", arrayToString(brandIds))
				.add("warehouseIds", arrayToString(warehouseIds))
				.add("warehouseProductItemMappingId",
						arrayToString(warehouseProductItemMappingId))
				.add("productItemIds", arrayToString(productItemIds))
				.add("visible", visible)
				.add("productitemtype", arrayToString(productItemTypes))
				.add("instock", inStock)
				.toString();
	}

	public static class Params {

		public static final String PRODUCT_ITEM_ID = "productitemid";
		public static final String MERCHANT_NAME = "merchantname";
		public static final String PRODUCT_ITEM_STATUS = "productitemstatus";
		public static final String MERCHANT_SKU = "merchantsku";
		public static final String MERCHANT_ID = "merchantid";
		public static final String MERCHANT_PRODUCT_ITEM_STATUS = "merchantproductitemstatus";
		public static final String KEYWORD = "keyword";
		public static final String MERCHANT_CODE = "merchantcode";
		public static final String WAREHOUSE_ID = "warehouseid";
		public static final String CATEGORY_ID = "categoryid";
		public static final String ROWS = "rows";
		public static final String START = "start";
		public static final String DATE_FROM_TO = "dateFromTo";
		public static final String WAREHOUSE_PRODUCT_ITEM_MAPPING_ID = "warehouseproductitemmappingid";
		public static final String BRAND_ID = "brandid";
		public static final String SORT = "sort";
		public static final String VISIBLE = "visible";
		public static final String PRODUCT_ITEM_TYPE = "productitemtype";
		public static final String IN_STOCK = "instock";
		public static final String APPROVED = "approved";
		public static final String SAFETY_STOCK = "safetystock";
		public static final String IS_ORIGINAL = "isoriginal";
	}

	public static class Default {

		public static final String KEYWORD = "*";
		private static final String ROWS = "10";
		private static final String START = "0";
		
	}
	
	public class InStock{
		public static final String TRUE = "-quantity:[* TO 0]";
		public static final String FALSE = "quantity:[* TO 0]";
	}

}
