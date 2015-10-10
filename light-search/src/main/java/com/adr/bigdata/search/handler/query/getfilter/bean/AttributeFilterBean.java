/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.bean;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author minhvv2
 *
 */
public class AttributeFilterBean extends AbstractQueryBean {
	private Map<Integer, List<String>> attId2Filter;

	public Map<Integer, List<String>> getAttId2Filter() {
		return attId2Filter;
	}

	public void setAttId2Filter(Map<Integer, List<String>> attId2Filter) {
		this.attId2Filter = attId2Filter;
	}

	public List<String> getFilterById(int attId) {
		return attId2Filter.get(attId);
	}

	/**
	 * for debugging purpose
	 */
	@Override
	public String toString() {
		String ret = "";
		for (Entry<Integer, List<String>> entry : attId2Filter.entrySet()) {
			ret += "[id:" + entry.getKey() + " filter:" + entry.getValue().toString() + "]";
		}
		return ret;
	}
}
