/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.indexing.db.sql.beans.MerchantBean;
import com.adr.bigdata.search.handler.db.sql.models.MerchantModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.query.NamedListHelper;
import com.adr.bigdata.search.handler.response.daos.GetFilterReturnField;

/**
 * @author minhvv2
 *
 */
public class MerchantWriter extends WriterStrategy implements DescriptableWriter {
	private MerchantExtractor extractor;

	public MerchantWriter() {
		model = ModelFactory.newInstance().getModel(MerchantModel.class);
		extractor = new MerchantExtractor();
	}

	@Override
	public void writeComponent(SolrQueryResponse response, NamedList facetCounts, Object... data) {
		assert data.length >= 1;
		SolrCache cache = (SolrCache) data[0];
		Map merchant2Count = extractor.transFormMerchantUsingSolrCache(facetCounts, cache);
		response.add(GetFilterReturnField.LIST_MERCHANT, NamedListHelper.convertMap(merchant2Count));
	}

	@Override
	public void writeInfor(SolrQueryResponse response, int merchantId, Object... data) {
		assert data.length >= 1;
		SolrCache cache = (SolrCache) data[0];
		String key = "merchant_" + merchantId;
		Object merchantInfor = cache.get(key);
		if (merchantInfor == null) {
			try {
				merchantInfor = ((MerchantModel) model).getMerchant(merchantId);
				cache.put(key, merchantInfor);
			} catch (Exception e) {
				// do nothing
			}
		}
		response.add(GetFilterReturnField.MERCHANT_NAME, ((MerchantBean) merchantInfor).getName());
		response.add(GetFilterReturnField.MERCHANT_SHORT_INFO, ((MerchantBean) merchantInfor).getInfo());
		response.add(GetFilterReturnField.MERCHANT_IMAGE, ((MerchantBean) merchantInfor).getImage());
		response.add(GetFilterReturnField.MERCHANT_ID, ((MerchantBean) merchantInfor).getId());
	}

	private class MerchantExtractor {

		@SuppressWarnings("unused")
		Map<MerchantBean, Integer> transFormMerchant(NamedList facetCounts) {
			Map<Integer, Integer> id2Count = genId2Count(facetCounts);
			if (id2Count.isEmpty()) {
				return Collections.EMPTY_MAP;
			}

			Collection<MerchantBean> merchantSet;
			try {
				merchantSet = ((MerchantModel) MerchantWriter.this.model).getAllMerchants(id2Count.keySet());
			} catch (Exception e) {
				return Collections.EMPTY_MAP;
			}
			Map<MerchantBean, Integer> result = new HashMap<MerchantBean, Integer>();
			for (MerchantBean bean : merchantSet) {
				result.put(bean, id2Count.get(bean.getId()));
			}
			return result;
		}

		Map<MerchantBean, Integer> transFormMerchantUsingSolrCache(NamedList facetCounts, SolrCache cache) {
			if (facetCounts == null) {
				return Collections.EMPTY_MAP;
			}
			NamedList facetField = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
			if (facetField == null) {
				return Collections.EMPTY_MAP;
			}
			NamedList merchantIds = (NamedList) facetField.get(FacetConstant.MERCHANT_ID_FACET);
			if (merchantIds == null) {
				return Collections.EMPTY_MAP;
			}

			Map<MerchantBean, Integer> result = new HashMap<>();
			Iterator<Map.Entry<String, Object>> it = merchantIds.iterator();
			MerchantModel model = (MerchantModel) MerchantWriter.this.model;
			while (it.hasNext()) {
				Map.Entry<String, Object> next = it.next();
				Integer merchantId = Integer.parseInt(next.getKey());
				Integer numFound = (Integer) next.getValue();
				Object merchantBean = cache.get("merchant_" + merchantId);
				if (merchantBean == null) {
					try {
						merchantBean = model.getMerchant(merchantId);
					} catch (Exception e) {
						//ignore
						//TODO: warning here
						continue;
					}
					cache.put("merchant_" + merchantId, merchantBean);
				}
				result.put((MerchantBean) merchantBean, numFound);
			}
			if (result.isEmpty())
				return Collections.EMPTY_MAP;

			return result;
		}

		private Map<Integer, Integer> genId2Count(NamedList facetCounts) {
			if (facetCounts == null) {
				return Collections.EMPTY_MAP;
			}
			NamedList facetField = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
			if (facetField == null) {
				return Collections.EMPTY_MAP;
			}
			NamedList merchantIds = (NamedList) facetField.get(FacetConstant.MERCHANT_ID_FACET);
			if (merchantIds == null) {
				return Collections.EMPTY_MAP;
			}

			Map<Integer, Integer> factedMerchantIds = new HashMap<>();
			Iterator<Map.Entry<String, Object>> it = merchantIds.iterator();
			while (it.hasNext()) {
				Map.Entry<String, Object> next = it.next();
				Integer merchantId = Integer.parseInt(next.getKey());
				Integer numFound = (Integer) next.getValue();
				factedMerchantIds.put(merchantId, numFound);
			}
			if (factedMerchantIds.isEmpty())
				return Collections.EMPTY_MAP;

			return factedMerchantIds;
		}
	}

}
