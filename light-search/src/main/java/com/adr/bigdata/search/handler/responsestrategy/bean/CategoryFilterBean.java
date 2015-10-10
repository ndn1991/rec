package com.adr.bigdata.search.handler.responsestrategy.bean;

import java.util.ArrayList;
import java.util.List;

public class CategoryFilterBean {

	private int attId;
	private String attName;
	private String unitName;

	private List<ObjectFilterBean> lvalue;

	public CategoryFilterBean(int attId, String attName, String unitName) {
		this.attId = attId;
		this.attName = attName;
		this.unitName = unitName;
		lvalue = new ArrayList<>();
	}

	public void addValue(ObjectFilterBean ofr) {
		lvalue.add(ofr);
	}

	public int getAttId() {
		return attId;
	}

	public void setAttId(int attId) {
		this.attId = attId;
	}

	public String getAttName() {
		return attName;
	}

	public void setAttName(String attName) {
		this.attName = attName;
	}

	public String getUnitName() {
		return unitName;
	}

	public void setUnitName(String unitName) {
		this.unitName = unitName;
	}

	public List<ObjectFilterBean> getLvalue() {
		return lvalue;
	}

	public void setLvalue(List<ObjectFilterBean> lvalue) {
		this.lvalue = lvalue;
	}
}
