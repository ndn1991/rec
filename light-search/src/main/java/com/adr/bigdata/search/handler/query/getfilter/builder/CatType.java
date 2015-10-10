/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.builder;

/**
 * @author minhvv2
 *
 */
public enum CatType {

	FIRST_CLASS(0) {
		@Override
		public String toString() {
			return "0";
		}
	},
	NORMAL_CAT(1) {
		@Override
		public String toString() {
			return "1";
		}		
	};

	private final int value;

	private CatType(int val) {
		value = val;
	}

	public int getValue() {
		return value;
	}
}
