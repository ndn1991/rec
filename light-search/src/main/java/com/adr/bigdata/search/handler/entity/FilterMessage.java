/**
 * 
 */
package com.adr.bigdata.search.handler.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * @author minhvv2
 *
 */
public class FilterMessage implements Message {
	private Map data;
	private int type;

	public FilterMessage() {
		this.data = new HashMap();
	}

	@Override
	public int getType() {
		return type;
	}

	@Override
	public void setType(int type) {
		this.type = type;
	}

	@Override
	public Map getData() {
		return this.data;
	}

	@Override
	public void setData(Map data) {
		this.data = data;
	}

}
