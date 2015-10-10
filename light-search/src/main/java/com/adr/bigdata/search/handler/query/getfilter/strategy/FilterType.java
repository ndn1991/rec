/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.strategy;

/**
 * @author minhvv2
 *
 */
public enum FilterType {
	ATT_FILTER() {
		@Override
		public String toString() {
			return "attFilter";
		}
	},
	BRAND_FILTER() {
		@Override
		public String toString() {
			return "brandFilter";
		}
	},
	CITY_FILTER() {
		@Override
		public String toString() {
			return "cityFilter";
		}
	},
	FEATURED_FILTER() {
		@Override
		public String toString() {
			return "featuredFilter";
		}
	},
	MERCHANT_FILTER() {
		@Override
		public String toString() {
			return "merchantFilter";
		}
	},
	PRICE_FILTER() {
		@Override
		public String toString() {
			return "priceFilter";
		}
	},
	CAT_FILTER() {
		@Override
		public String toString() {
			return "catFilter";
		}
	}
}