/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.strategy;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.adr.bigdata.search.handler.db.sql.models.AttributeModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.query.getfilter.bean.AttributeFilterBean;
import com.adr.bigdata.search.handler.utils.SolrParamHelper;
import com.google.common.base.Joiner;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
public class AttFilter extends AbstractFilterStrategy implements Loggable {
	private final AttributeModel attModel;

	public AttFilter() {
		ModelFactory modelFactory = ModelFactory.newInstance();
		this.attModel = modelFactory.getModel(AttributeModel.class);
	}

	@Override
	public void doFilter(Object... data) {
		assert data.length >= 2;
		AttributeFilterBean filterBean = (AttributeFilterBean) data[0];
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[1];
		if (filterBean != null) {
			filterBean
					.getAttId2Filter()
					.entrySet()
					.stream()
					.forEach(
							(entry) -> {
								String attributeField = "attr_" + entry.getKey() + "_txt";
								SolrParamHelper.writeTaggedFacet(solrParams, attributeField, "{!ex=" + attributeField
										+ "}" + attributeField);
								solrParams.add(CommonParams.FQ, genAttFilterElem(attributeField, entry.getValue()));
							});
		}
	}

	private String genAttFilterElem(String attributeField, List<String> filter) {
		List<String> fq = new LinkedList<>();
		for (String f : filter) {
			fq.add(attributeField + ":\"" + f + "\"");
		}
		String ret = "";
		ret = "{!tag=" + attributeField + "}" + Joiner.on(" OR ").join(fq);
		//TODO: minhvv2 - clearify business logic here. isText problem	
		return ret;
	}

	@Override
	public void doFacet(Object... data) {
		assert data.length >= 2;
		Integer catId = (Integer) data[0];
		ModifiableSolrParams solrParams = (ModifiableSolrParams) data[1];
		FilterMessage mess = (FilterMessage) data[2];
		SolrCache cache = (SolrCache) data[3];
		Map<Integer, AttributeCategoryMappingBean> attId2AttBean = null;
		try {
			String key = "catAtt_" + catId;
			attId2AttBean = (Map<Integer, AttributeCategoryMappingBean>) cache.get(key);
			if (attId2AttBean == null) {
				attId2AttBean = attModel.getAttributesMapByCat(catId);
				cache.put(key, attId2AttBean);
			}
		} catch (Exception e) {
			getLogger().error("error building att facet for second class category...." + e.getMessage());
		}
		if (attId2AttBean != null) {
			mess.getData().put("attMapping", attId2AttBean);
			for (AttributeCategoryMappingBean bean : attId2AttBean.values()) {
				String facetField = "attr_" + bean.getAttributeId() + "_txt";
				String facetLimited = "f.attr_" + bean.getAttributeId() + "_txt.facet.limit";
				solrParams.set(facetLimited, "10");
				solrParams.add("facet.field", facetField);
			}
		}
	}
}
