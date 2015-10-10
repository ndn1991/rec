/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.response.daos.GetFilterReturnField;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;

/**
 * @author minhvv2
 *
 */
public class FeaturedWriter extends WriterStrategy {

	@Override
	public void writeComponent(SolrQueryResponse response, NamedList facetCounts, Object... data) {
		SolrQueryResponseHelper.write2Response(response, GetFilterReturnField.LIST_FEATURED,
				createListFeatured(facetCounts));
	}

	private List<NamedList> createListFeatured(NamedList facetCounts) {

		List<NamedList> lstFeatured = new ArrayList<>();
		if (facetCounts == null) {
			return lstFeatured;
		}
		NamedList facetQuery = (NamedList) facetCounts.get(FacetConstant.FACET_QUERIES);
		if (facetQuery == null) {
			return lstFeatured;
		}

		Integer newArrived = (Integer) facetQuery.getVal(0);
		Integer promotioned = (Integer) facetQuery.getVal(1);

		lstFeatured.add(createFeatured("isPromotion", promotioned));
		lstFeatured.add(createFeatured("newArrival", newArrived));

		return lstFeatured;
	}

	private NamedList createFeatured(String featuredName, Integer numFound) {
		NamedList ret = new SimpleOrderedMap<>();
		ret.add("featuredName", featuredName);
		ret.add("numFound", numFound);
		return ret;
	}

}
