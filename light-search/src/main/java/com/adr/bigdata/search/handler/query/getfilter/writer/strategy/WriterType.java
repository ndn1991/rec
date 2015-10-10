/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

/**
 * @author minhvv2
 *
 */
public enum WriterType {
	ATT_WRITER() {
		@Override
		public String toString() {
			return "attFilter";
		}
	},
	BRAND_WRITER() {
		@Override
		public String toString() {
			return "brandFilter";
		}
	},
	CITY_WRITER() {
		@Override
		public String toString() {
			return "cityFilter";
		}
	},
	FEATURED_WRITER() {
		@Override
		public String toString() {
			return "featuredFilter";
		}
	},
	MERCHANT_WRITER() {
		@Override
		public String toString() {
			return "merchantFilter";
		}
	},
	PRICE_WRITER() {
		@Override
		public String toString() {
			return "priceFilter";
		}
	},
	CAT_WRITER() {
		@Override
		public String toString() {
			return "catFilter";
		}
	},
	CATTREE_WRITER() {
		@Override
		public String toString() {
			return "catTreeFilter";
		}
	}
}
