/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.query;

import java.util.LinkedList;
import java.util.List;

import com.google.api.client.util.Strings;
import com.google.common.base.Joiner;

/**
 *
 * @author Tong Hoang Anh
 */
@SuppressWarnings({})
public class AttributeFilter {

	private String attributeId;
	private String attributeName;
	private final List<String> filter;
	private boolean isText;
	private String unitName;
	private int attributeType;
	private String filterSpan;

	public AttributeFilter(String attributeId, String attributeName, boolean isText, String unitName,
			int attributeType, String filterSpan) {
		this.attributeId = attributeId;
		this.attributeName = attributeName;
		filter = new LinkedList<>();
		this.isText = isText;
		this.unitName = unitName;
		this.attributeType = attributeType;
		this.filterSpan = filterSpan;
	}

	public AttributeFilter(String attributeId) {
		filter = new LinkedList<>();
		if (attributeId.contains("_")) {
			String[] attId2Type = attributeId.split("_");
			if (attId2Type.length == 2) {
				this.attributeId = attId2Type[0];
				this.attributeType = Integer.valueOf(attId2Type[1]);
				if (this.attributeType == 2) {
					isText = true;
				} else {
					isText = false;
				}
			}
		} else {
			this.attributeId = attributeId;
			isText = true;
		}
	}

	public void addFilter(String filter) {
		if (filter.contains("TO")) {
			this.filter.add("[" + filter + "]");
			//			isText = false;
			return;
		}
		//		try {
		//			Double.parseDouble(filter);
		//			//hidden error
		//			isText = false;
		//		} catch (Exception e) {
		//		}
		this.filter.add(filter);
	}

	public String getFilterQuery() {
		String attributeField = toAttributeField();
		List<String> fq = new LinkedList<>();
		for (String f : filter) {
			fq.add(attributeField + ":\"" + f + "\"");
		}
		String ret = "";
		//		ret = "{!tag=attr_" + attributeId + "_txt}" + Joiner.on(" OR ").join(fq);

		//TODO: minhvv2 - clearify bussiness logic here.
		if (isText) {
			ret = "{!tag=attr_" + attributeId + "_txt}" + Joiner.on(" OR ").join(fq);
		} else {
			ret = "{!tag=attr_" + attributeId + "_d}" + Joiner.on(" OR ").join(fq);
		}
		return ret;
	}

	public String toAttributeField() {
		//		return "attr_" + attributeId + "_txt";

		//TODO: minhvv2 - remove double attribute type
		if (isText) {
			return "attr_" + attributeId + "_txt";
		} else {
			return "attr_" + attributeId + "_d";
		}
	}

	public String getAttributeId() {
		return attributeId;
	}

	public boolean isIsText() {
		return isText;
	}

	public boolean isRange() {
		return this.attributeType == TYPE.RANGE && Strings.isNullOrEmpty(this.filterSpan.trim());
	}

	public String getUnitName() {
		return unitName;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public int getAttributeType() {
		return attributeType;
	}

	public String getFilterSpan() {
		return filterSpan;
	}

	public static class TYPE {

		public final static int RANGE = 4;
		public final static int VALUE = 2;
	}
}
