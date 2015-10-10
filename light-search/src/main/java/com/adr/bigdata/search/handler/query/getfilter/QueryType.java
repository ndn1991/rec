package com.adr.bigdata.search.handler.query.getfilter;

public enum QueryType {
	BROWSE_BY_CAT() {
		@Override
		public String toString() {
			return "1";
		}
	},
	BROWSE_BY_MERCHANT() {
		@Override
		public String toString() {
			return "2";
		}
	},
	BROWSE_BY_BRAND() {
		@Override
		public String toString() {
			return "3";
		}
	},
	FIRST_CLASS_SEARCH() {
		@Override
		public String toString() {
			return "4";
		}
	},
	NORMAL_SEARCH() {
		@Override
		public String toString() {
			return "5";
		}
	}
}