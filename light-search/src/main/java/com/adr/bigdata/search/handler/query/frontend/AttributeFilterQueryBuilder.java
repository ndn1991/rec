package com.adr.bigdata.search.handler.query.frontend;

import java.util.Map;

import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.adr.bigdata.search.handler.db.sql.models.AttributeModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.nhb.common.Loggable;

public class AttributeFilterQueryBuilder implements Loggable {

	public AttributeFilterQueryBuilder() {
		ModelFactory modelFactory = ModelFactory.newInstance();
		this.attributeModel = modelFactory.getModel(AttributeModel.class);
	}

	private int categoryId;
	private AttributeModel attributeModel;

	public AttributeModel getAttributeModel() {
		return attributeModel;
	}

	public void setAttributeModel(AttributeModel attributeModel) {
		this.attributeModel = attributeModel;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public void buildAttributeFacet(ModifiableSolrParams solrParams, String categoryId, SolrQueryResponse response) {
		int catId = Integer.valueOf(categoryId);
		Map<Integer, AttributeCategoryMappingBean> attId2AttBean = null;
		try {
			attId2AttBean = attributeModel.getAttributesMapByCat(catId);
		} catch (Exception e) {
			getLogger().error("error buildAttributeFacet...." + e.getMessage());
		}

		response.add("attributeCached", attId2AttBean);
		if (attId2AttBean != null) {
			for (AttributeCategoryMappingBean bean : attId2AttBean.values()) {
				//TODO: hidden error. Only attribute type=4 is double type
				if (bean.getAttributeType() == 4) {

					addAttFacetField(solrParams, bean.getAttributeId(), "_d");
				} else {
					addAttFacetField(solrParams, bean.getAttributeId(), "_txt");
				}

				//				if (bean.getFilterSpan() == null || !bean.getFilterSpan().isEmpty()) {
				//					addAttFacetField(solrParams, bean.getAttributeId(), "_txt");
				//				} else {
				//					addAttFacetField(solrParams, bean.getAttributeId(), "_d");
				//				}
			}
		}
	}

	public void buildCachedFacet(ModifiableSolrParams solrParams,
			Map<Integer, AttributeCategoryMappingBean> cachedAttId2AttBean, SolrQueryResponse response) {
		response.add("attributeCached", cachedAttId2AttBean);
		if (cachedAttId2AttBean != null) {
			for (AttributeCategoryMappingBean bean : cachedAttId2AttBean.values()) {
				//TODO: hidden error. Only attribute type=4 is double type
				if (bean.getAttributeType() == 4) {

					addAttFacetField(solrParams, bean.getAttributeId(), "_d");
				} else {
					addAttFacetField(solrParams, bean.getAttributeId(), "_txt");
				}

				//				if (bean.getFilterSpan() == null || !bean.getFilterSpan().isEmpty()) {
				//					addAttFacetField(solrParams, bean.getAttributeId(), "_txt");
				//				} else {
				//					addAttFacetField(solrParams, bean.getAttributeId(), "_d");
				//				}
			}
		}
	}

	private void addAttFacetField(ModifiableSolrParams solrParams, int attId, String attType) {
		String facetField = "attr_" + attId + attType;
		//	 remove merchant, brand, attribute filter limit since sprint 6. The default value of SOLR is 100	
		//		String facetLimited = "f.attr_" + attId + attType + ".facet.limit";
		//		solrParams.add(facetLimited, "10");
		solrParams.add("facet.field", facetField);
	}

}
