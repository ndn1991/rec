/**
 * 
 */
package com.adr.bigdata.search.handler.responsestrategy;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.transform.TransformContext;
import org.apache.solr.search.DocList;

import com.adr.bigdata.search.handler.responsestrategy.bean.ComboSearchBean;
import com.adr.bigdata.search.handler.utils.SolrDocumentUtils;
import com.google.common.collect.Sets;

/**
 * @author minhvv2
 *
 */
public class ComboSearchResponseStrategy implements ResponseStrategy {

	@Override
	public void execute(SolrQueryResponse rsp, SolrQueryRequest req) {
		ResultContext response = (ResultContext) rsp.getValues().get("response");
		rsp.getValues().remove("response");
		if (response != null) {
			try {
				SolrDocumentList docList = SolrDocumentUtils.getSolrDocumentList(response.docs, rsp, req);
				rsp.add("listProduct", docList);
			} catch (IOException e) {
				getLogger().error("ERROR getting solr document list.....{}", e.getStackTrace());
			}
		}
	}

	public void handleResponse(SolrQueryResponse rsp, SolrQueryRequest req, List<String> productIds) {
		ResultContext response = (ResultContext) rsp.getValues().get("response");
		rsp.getValues().remove("response");
		if (response != null) {
			try {
				List<NamedList> docList = getListProductResponse(response.docs, rsp, req, productIds);
				rsp.add("listProduct", docList);
			} catch (IOException e) {
				getLogger().error("ERROR getting solr document list.....{}", e.getStackTrace());
			}
		}
	}

	private List<NamedList> getListProductResponse(DocList docs, SolrQueryResponse rsp, SolrQueryRequest req,
			List<String> productIds) throws IOException {
		TransformContext context = new TransformContext();
		context.req = req;
		context.searcher = req.getSearcher();
		context.iterator = docs.iterator();
		int sz = docs.size();
		Set<String> fnames = rsp.getReturnFields().getLuceneFieldNames();
		Map<String, List<ComboSearchBean>> processingProductId = new HashMap<String, List<ComboSearchBean>>();
		for (int i = 0; i < sz; i++) {
			int id = context.iterator.nextDoc();
			Document doc = context.searcher.doc(id, fnames);
			String productId = doc.get("product_id");
			final HashSet<String> finalCity = mergeReceiveCityAndServedProvince(
					new HashSet(Arrays.asList(doc.getValues("received_city_id"))),
					new HashSet(Arrays.asList(doc.getValues("served_province_ids"))));
			final HashSet<String> servedDistrictIds = new HashSet(Arrays.asList(doc.getValues("served_district_ids")));

			ComboSearchBean bean = new ComboSearchBean(doc.get("product_item_id"));

			if (!processingProductId.containsKey(productId)) {
				List<ComboSearchBean> lstProductItems = new ArrayList<>();
				bean.setCityIds(finalCity);
				bean.setDistrictIds(servedDistrictIds);
				lstProductItems.add(bean);
				processingProductId.put(productId, lstProductItems);
			} else {
				List<ComboSearchBean> currentListProductItem = processingProductId.get(productId);
				boolean contains = false;
				for (ComboSearchBean item : currentListProductItem) {
					if (item.equals(bean)) {
						contains = true;
						//merge cityId and districtId;							
						item.getCityIds().addAll(finalCity);
						item.getDistrictIds().addAll(servedDistrictIds);
						break;
					}
				}
				if (!contains) {
					bean.setCityIds(finalCity);
					bean.setDistrictIds(servedDistrictIds);
					currentListProductItem.add(bean);
				}
			}
		}
		return reStructureMaptoNamedList(productIds, processingProductId);
	}

	private List<NamedList> reStructureMaptoNamedList(List<String> productIds,
			Map<String, List<ComboSearchBean>> processingProductId) {
		List<NamedList> returnProductList = new ArrayList<NamedList>();
		for (String productId : productIds) {
			NamedList productIdEntity = new SimpleOrderedMap<>();
			productIdEntity.add("productid", productId);
			List<ComboSearchBean> lstExtractedProductItems = processingProductId.getOrDefault(productId, null);
			if (lstExtractedProductItems != null) {
				List<NamedList> listProductItems = new ArrayList<>();
				for (ComboSearchBean bean : lstExtractedProductItems) {
					NamedList productItemEntity = new SimpleOrderedMap<>();
					productItemEntity.add("productItemId", bean.getProductItemId());
					productItemEntity.add("servedprovinceids", bean.getCityIds());// provinceId and cityId are the same
					if (bean.getDistrictIds().contains("0")) {
						productItemEntity.add("serveddistrictids", Sets.newHashSet("0"));
					} else {
						productItemEntity.add("serveddistrictids", bean.getDistrictIds());
					}
					listProductItems.add(productItemEntity);
				}
				productIdEntity.add("listProductItemId", listProductItems);
			} else {
				productIdEntity.add("listProductItemId", null);
			}
			returnProductList.add(productIdEntity);
		}
		return returnProductList;
	}

	private HashSet<String> mergeReceiveCityAndServedProvince(HashSet<String> receivedCity,
			HashSet<String> servedProvince) {
		if (receivedCity != null) {
			receivedCity.remove("0");
			if (servedProvince.contains("0")) {
				return receivedCity;
			} else {
				return new HashSet<>(Sets.intersection(receivedCity, servedProvince));
			}
		} else {
			return servedProvince;
		}
	}
}
