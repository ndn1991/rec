package com.adr.bigdata.search.handler.response.daos;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.solr.common.util.NamedList;

import com.adr.bigdata.search.handler.utils.CommonConstant;

public class SolrResponseHelper {
	private static final Pattern PATTERN = Pattern.compile("attr_(\\d+)_.*");

	/*
	 * fieldName is in attr_id_txt format
	 */
	public static int convertAttributeFieldName2AttId(String fieldName) {
		Matcher matcher = PATTERN.matcher(fieldName);
		if (!matcher.find()) {
			throw new NumberFormatException("the fieldName must in attr_id_txt format");
		}
		String strAttId = matcher.group(1);
		return Integer.valueOf(strAttId);
	}

	/**
	 * 
	 * @param stats
	 * @return
	 */
	public static Object extractPriceFromStatsNamedList(NamedList stats) {
		Object maxPrice = -1;
		NamedList statField = (NamedList) stats.get(CommonConstant.STATS_FIELDS);
		if (statField != null) {
			NamedList sellPriceStatObject = (NamedList) statField.get("sell_price");
			if (sellPriceStatObject != null) {
				maxPrice = sellPriceStatObject.get("max");
				if (maxPrice == null) {
					maxPrice = -1;
				}
			}
		}
		return maxPrice;
	}
}
