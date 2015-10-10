package com.adr.bigdata.search.handler.query.frontend;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.junit.Test;

public class FrontEndQueryBuilderTest {

	@Test
	public void testSysDate() {
		String now = String.valueOf(System.currentTimeMillis());
		String promotionQuery = "is_promotion_mapping:true AND is_promotion:true AND start_time_discount:[* TO " + now
				+ "] AND finish_time_discount:[" + now + " TO *]";

		System.out.println("now..." + now);
	}

	@Test
	public void setPriceFilterTest() {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		FrontEndQueryBuilder builder = new FrontEndQueryBuilder();

		FrontEndQuery query = new FrontEndQuery();
		query.setPrices("10 TO 100");

		List<String> filter = new LinkedList<>();

		builder.setPriceFilter(query, filter, solrParams);

		String result = filter.get(0);
		String exp = "{!frange l=10  u= 100}$price";

		assertEquals(exp, result);
	}

	@Test
	public void setPriceFilterTest1() {
		ModifiableSolrParams solrParams = new ModifiableSolrParams();
		FrontEndQueryBuilder builder = new FrontEndQueryBuilder();

		FrontEndQuery query = new FrontEndQuery();
		query.setPrices("10");

		List<String> filter = new LinkedList<>();

		builder.setPriceFilter(query, filter, solrParams);

		String result = filter.get(0);
		String exp = "{!frange l=10 u=10}$price";

		assertEquals(exp, result);
	}

}
