package com.adr.bigdata.search.handler.responsestrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.query.deal.DealFields;
import com.adr.bigdata.search.handler.utils.PriceUtils;

public class DealResponseExecutor implements ResponseStrategy {
	public static final int NUM_ELEMENT_PER_CAT = 4;

	private List<SimpleOrderedMap<Object>> listPromotion;

	public DealResponseExecutor() {
		String sPromotion = System.getProperty("deal.promotions", "0-10,10-20,20-30,30-40,40-100");
		String[] sPromotions = sPromotion.split(",");
		listPromotion = new ArrayList<SimpleOrderedMap<Object>>();
		for (int i = 0; i < sPromotions.length; i++) {
			String[] minMax = sPromotions[i].split("-");
			SimpleOrderedMap<Object> doc = new SimpleOrderedMap<Object>();
			doc.add("minpromotion", minMax[0]);
			doc.add("maxpromotion", minMax[1]);
			listPromotion.add(doc);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		ResultContext response = (ResultContext) rsp.getValues().get("response");
		rsp.getValues().remove("response");
		if (response != null) {
			try {
				rsp.add("listDeal", response.docs);
				rsp.add("numFound", response.docs.matches());
			} catch (Exception e) {
				rsp.add("listDeal", new NamedList<Object>());
				rsp.add("numFound", 0);
			}
		} else {
			rsp.add("listDeal", new NamedList<Object>());
			rsp.add("numFound", 0);
		}

		NamedList facetCounts = (NamedList) rsp.getValues().get("facet_counts");
		rsp.getValues().remove("facet_counts");
		if (facetCounts != null) {
			NamedList facetFields = (NamedList) facetCounts.get("facet_fields");
			if (facetFields != null) {
				// Extract category leaf filter
				NamedList catFacet = (NamedList) facetFields.get(DealFields.CAT_FACET);
				TreeMap<Long, SimpleOrderedMap> orderedCatFacet = new TreeMap<Long, SimpleOrderedMap>();
				for (int i = 0; i < catFacet.size(); i++) {
					String key = catFacet.getName(i);
					String[] keyElements = key.split("_", NUM_ELEMENT_PER_CAT);
					getLogger().debug("key: " + key);
					if (keyElements.length >= NUM_ELEMENT_PER_CAT) {
						int catId = 0;
						try {
							catId = Integer.parseInt(keyElements[0]);
						} catch (Exception e) {
						}
						String catStatus = keyElements[1];
						int catSort = 0;
						try {
							catSort = Integer.parseInt(keyElements[2]);
						} catch (Exception e) {
						}
						String catName = keyElements[3];
						if ("1".equalsIgnoreCase(catStatus)) {
							SimpleOrderedMap map = new SimpleOrderedMap();
							map.add("categoryid", catId);
							map.add("categoryname", catName);
							map.add("numfound", catFacet.getVal(i));
							map.add("sort", catSort);

							long sort = (((long) catSort) << Integer.BYTES) | ((long) catId);
							orderedCatFacet.put(sort, map);
						}
					}
				}
				rsp.add("listCategoryLeaf", orderedCatFacet.values());

				// Extract category 3 filter
				NamedList cat3Facet = (NamedList) facetFields.get(DealFields.CAT_3_FACET);
				TreeMap<Long, SimpleOrderedMap> orderedCat3Facet = new TreeMap<Long, SimpleOrderedMap>();
				for (int i = 0; i < cat3Facet.size(); i++) {
					String key = cat3Facet.getName(i);
					String[] keyElements = key.split("_", NUM_ELEMENT_PER_CAT);
					getLogger().debug("key: " + key);
					if (keyElements.length >= NUM_ELEMENT_PER_CAT) {
						int catId = 0;
						try {
							catId = Integer.parseInt(keyElements[0]);
						} catch (Exception e) {
						}
						String catStatus = keyElements[1];
						int catSort = 0;
						try {
							catSort = Integer.parseInt(keyElements[2]);
						} catch (Exception e) {
						}
						String catName = keyElements[3];
						if ("1".equalsIgnoreCase(catStatus)) {
							SimpleOrderedMap map = new SimpleOrderedMap();
							map.add("categoryid", catId);
							map.add("categoryname", catName);
							map.add("numfound", cat3Facet.getVal(i));
							map.add("sort", catSort);

							long sort = (((long) catSort) << Integer.BYTES) | ((long) catId);
							orderedCat3Facet.put(sort, map);
						}
					}
				}
				rsp.add("listCategory", orderedCat3Facet.values());

				// Extract destination filter
				NamedList desIds = (NamedList) facetFields.get(DealFields.DESTINATION_FACETS);
				List<SimpleOrderedMap> listDestination = new ArrayList<SimpleOrderedMap>();
				for (int i = 0; i < desIds.size(); i++) {
					String key = desIds.getName(i);
					String[] keyElements = key.split("_");
					if (keyElements.length >= 4) {
						String desId = keyElements[0];
						String desStatus = keyElements[1];
						String desMappingStatus = keyElements[2];
						if ("1".equalsIgnoreCase(desStatus) && "1".equalsIgnoreCase(desMappingStatus)) {
							SimpleOrderedMap doc = new SimpleOrderedMap();
							doc.add("destinationid", desId);
							int beginIndex = desId.length() + desStatus.length() + desMappingStatus.length() + 3;
							doc.add("destinationname", key.substring(beginIndex));
							doc.add("numfound", desIds.getVal(i));

							listDestination.add(doc);
						}
					}
				}
				rsp.add("listDestination", listDestination);
			}
		}
		// Extract price filter
		NamedList statsNamedList = (NamedList) rsp.getValues().get("stats");
		rsp.getValues().remove("stats");
		NamedList statsFieldsNamedList = (NamedList) statsNamedList.get("stats_fields");
		NamedList sellPriceNamedList = (NamedList) statsFieldsNamedList.get(DealFields.SELL_PRICE);
		Double dMaxSellPrice = (Double) sellPriceNamedList.getVal(0);
		if (dMaxSellPrice != null) {
			int maxSellPrice = dMaxSellPrice.intValue();
			int numRange = Integer.parseInt(System.getProperty("deal.price.num", "5"));
			int minPrice = Integer.parseInt(System.getProperty("deal.price.min", "0"));
			int[][] ranges = PriceUtils.getRanges(maxSellPrice, minPrice, numRange);
			List<SimpleOrderedMap<Object>> listPrice = new ArrayList<SimpleOrderedMap<Object>>();
			for (int i = 0; i < ranges.length; i++) {
				SimpleOrderedMap<Object> doc = new SimpleOrderedMap<Object>();
				doc.add("min", ranges[i][0]);
				doc.add("max", ranges[i][1]);
				listPrice.add(doc);
			}
			rsp.add("listPrice", listPrice);
		}

		// Extract promotion filter
		rsp.add("listPromotion", listPromotion);
	}

}
