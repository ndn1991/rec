/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.strategy;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.google.api.client.util.Strings;

/**
 * @author minhvv2
 *         <p>
 *         Price filter. Input is a String with this format: price=0 TO 1000000.
 *         The input is assumed to be correct
 *         </p>
 */
public class PriceFilter extends AbstractFilterStrategy {
	private static final String PRICE_LOCAL_PARAM = "if(and(and(and(max(sub(ms(NOW), start_time_discount), 0), max(sub(finish_time_discount,ms(NOW)), 0)), is_promotion),is_promotion_mapping), promotion_price, sell_price)";

	@Override
	public void doFilter(Object... data) {
		assert data.length >= 2;// make sure to pass correct arg
		String price = (String) data[0];
		if (Strings.isNullOrEmpty(price)) {
			return;
		}
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[1];
		solrParams.set("price", PRICE_LOCAL_PARAM);
		String[] splitted = price.split("TO");
		if (splitted.length == 2) {
			String lower = splitted[0];
			String upper = splitted[1];
			//$price already define in collapse
			String priceFilter = String.format("{!frange l=%s u=%s}$price", lower, upper);
			solrParams.add(CommonParams.FQ, priceFilter);
		} else {
			String lower = price;
			String upper = price;
			String priceFilter = String.format("{!frange l=%s u=%s}$price", lower, upper);
			solrParams.add(CommonParams.FQ, priceFilter);
		}
	}

	@Override
	public void doFacet(Object... data) {
		assert data.length >= 1; // pass enough arg
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[0];
		solrParams.set("stats", "true");
		solrParams.set("stats.field", "sell_price");
	}

}
