package com.adr.bigdata.search.handler.utils;

public class PriceUtils {
	public static int getGap(int maxPrice, int minPrice, int numRangePrice) {
		if (maxPrice - minPrice > 0 && numRangePrice > 0) {
			int r = 10000;
			if ((maxPrice - minPrice) > 1000000000) {
				r = 100000000;
			} else if ((maxPrice - minPrice) > 100000000) {
				r = 10000000;
			} else if ((maxPrice - minPrice) > 10000000) {
				r = 1000000;
			} else if ((maxPrice - minPrice) > 500000) {
				r = 100000;
			} else {
				r = 50000;
			}
			int gap = ((maxPrice - minPrice) / numRangePrice) / r;
			if ((gap * r * numRangePrice) < (maxPrice - minPrice)) {
				gap++;
			}
			gap = Math.max(gap, 1);
			gap = gap * r;

			return gap;
		}
		throw new IllegalArgumentException("minPrice >= maxPrice || numRangePrice <= 0");
	}

	public static int[][] getRanges(int maxPrice, int minPrice, int numRangePrice) {
		int gap = getGap(maxPrice, minPrice, numRangePrice);
		int[][] result = new int[numRangePrice][];
		for (int i = 0; i < numRangePrice; i++) {
			int[] element = new int[2];
			element[0] = i * gap;
			element[1] = element[0] + gap;
			result[i] = element;
		}
		return result;
	}
}
