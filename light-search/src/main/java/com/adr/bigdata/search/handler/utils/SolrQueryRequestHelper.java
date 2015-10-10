/**
 * 
 */
package com.adr.bigdata.search.handler.utils;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.firstclassfunction.Function;
import com.google.api.client.util.Strings;

/**
 * @author minhvv2
 *
 */
public class SolrQueryRequestHelper {

	public static <T> List<T> extractListParams(SolrQueryRequest request, String paramName, String separator,
			Function<String, T> transformer) {
		String strListProductIds = request.getParams().get(paramName);
		List<T> result = new ArrayList<>();
		if (!Strings.isNullOrEmpty(strListProductIds)) {
			String[] splitedProductIds = strListProductIds.split(separator);
			for (String productId : splitedProductIds) {
				result.add(transformer.apply(productId));
			}
		}
		return result;
	}
}
