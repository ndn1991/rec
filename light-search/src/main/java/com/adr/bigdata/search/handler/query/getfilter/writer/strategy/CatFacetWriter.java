/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.search.handler.db.sql.models.CategoryModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;
import com.adr.bigdata.search.handler.query.NamedListHelper;
import com.adr.bigdata.search.handler.response.daos.GetFilterReturnField;

/**
 * @author minhvv2
 *
 */
public class CatFacetWriter extends WriterStrategy {

	public CatFacetWriter() {
		model = ModelFactory.newInstance().getModel(CategoryModel.class);
	}

	@Override
	public void writeComponent(SolrQueryResponse response, NamedList facetCount, Object... data) {
		Map<Integer, Integer> id2Count = genId2Count(facetCount);
		assert data.length >= 1;
		SolrCache cache = (SolrCache) data[0];
		List<CategoryBean> catBeans = transformCatId2CatName(id2Count.keySet(), cache);
		SolrQueryResponseHelper.write2Response(response, GetFilterReturnField.LIST_CATEGORY,
				NamedListHelper.convertList(catBeans, id2Count));
	}

	private List<CategoryBean> transformCatId2CatName(Set<Integer> lstIds, SolrCache cache) {
		List<CategoryBean> catBeans = new ArrayList<CategoryBean>();
		CategoryModel catModel = (CategoryModel) model;
		for (Integer catId : lstIds) {
			String key = "catBean_" + catId;
			Object catBean = cache.get(key);
			if (catBean == null) {
				catBean = catModel.getCategory(catId);
				cache.put(key, catBean);
			}
			catBeans.add((CategoryBean) catBean);
		}
		return catBeans;
	}

	private Map<Integer, Integer> genId2Count(NamedList facetCounts) {
		if (facetCounts == null) {
			return Collections.EMPTY_MAP;
		}
		NamedList facetField = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
		if (facetField == null) {
			return Collections.EMPTY_MAP;
		}
		NamedList categoryIds = (NamedList) facetField.get(FacetConstant.CATEGORY_ID_FACET);
		if (categoryIds == null) {
			return Collections.EMPTY_MAP;
		}

		Map<Integer, Integer> factedCatIds = new HashMap<>();
		Iterator<Map.Entry<String, Object>> it = categoryIds.iterator();
		while (it.hasNext()) {
			Map.Entry<String, Object> next = it.next();
			Integer catId = Integer.parseInt(next.getKey());
			Integer numFound = (Integer) next.getValue();
			factedCatIds.put(catId, numFound);
		}
		if (factedCatIds.isEmpty())
			return Collections.EMPTY_MAP;

		return factedCatIds;
	}

}
