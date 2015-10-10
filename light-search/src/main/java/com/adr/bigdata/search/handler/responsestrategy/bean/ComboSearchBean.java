/**
 * 
 */
package com.adr.bigdata.search.handler.responsestrategy.bean;

import java.util.HashSet;

/**
 * @author minhvv2
 *
 */
public class ComboSearchBean {
	private String productItemId;
	private HashSet<String> cityIds;
	private HashSet<String> districtIds;
	private String warehouseId;

	public ComboSearchBean(String productItemId) {
		this.productItemId = productItemId;
	}

	public String getProductItemId() {
		return productItemId;
	}

	public void setProductItemId(String productItemId) {
		this.productItemId = productItemId;
	}

	public HashSet<String> getCityIds() {
		return cityIds;
	}

	public void setCityIds(HashSet<String> cityIds) {
		this.cityIds = cityIds;
	}

	public HashSet<String> getDistrictIds() {
		return districtIds;
	}

	public void setDistrictIds(HashSet<String> districtIds) {
		this.districtIds = districtIds;
	}

	public String getWarehouseId() {
		return warehouseId;
	}

	public void setWarehouseId(String warehouseId) {
		this.warehouseId = warehouseId;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj instanceof ComboSearchBean) {
			return ((ComboSearchBean) obj).productItemId.equals(this.productItemId);
		}
		return false;
	}

}
