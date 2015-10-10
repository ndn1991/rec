package com.adr.bigdata.search.handler.responsestrategy.bean;

public class ObjectFilterBean {
	private String value;
	private long numFound;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public long getNumFound() {
		return numFound;
	}

	public void setNumFound(long numFound) {
		this.numFound = numFound;
	}

}
