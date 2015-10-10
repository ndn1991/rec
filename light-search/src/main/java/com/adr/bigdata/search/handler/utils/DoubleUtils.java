/**
 * 
 */
package com.adr.bigdata.search.handler.utils;

import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;

/**
 * @author minhvv2
 *
 */
public class DoubleUtils {
	public static String transform(Double doubleVal, String unit) {
		Integer accuracy = Integer.valueOf(System.getProperty("getfilter.attribute.doubleaccuracy", "3"));
		String strAccuracy = StringUtils.repeat("#", accuracy);
		DecimalFormat format = new DecimalFormat("0." + strAccuracy);
		String result = format.format(doubleVal);
		return (result + " " + unit.trim());
	}

	public static String formatDouble(Double val) {
		DecimalFormat format = new DecimalFormat("0.###");
		return format.format(val);
	}
}