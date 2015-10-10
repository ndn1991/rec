/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.adr.bigdata.search.handler.db.sql.models.AttributeModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.response.daos.GetFilterReturnField;
import com.adr.bigdata.search.handler.response.daos.SolrResponseHelper;
import com.adr.bigdata.search.handler.utils.DoubleUtils;
import com.google.common.base.Strings;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
public class AttributeWriter extends WriterStrategy implements Loggable {
	public AttributeWriter() {
		this.model = ModelFactory.newInstance().getModel(AttributeModel.class);
	}

	@Override
	public void writeComponent(SolrQueryResponse response, NamedList facetCount, Object... data) {
		assert data.length >= 2;
		int catId = (int) data[0];
		Map<Integer, AttributeCategoryMappingBean> cat2Attribute = (Map<Integer, AttributeCategoryMappingBean>) data[1];
		SolrCache cache = (SolrCache) data[2];
		try {
			writeAttributeFacetResult(response, facetCount, catId, cat2Attribute, cache);
		} catch (Exception e) {
			// do nothing
		}
	}

	//TODO: refactor this
	private void writeAttributeFacetResult(SolrQueryResponse rsp, NamedList facetCounts, int catId,
			Map<Integer, AttributeCategoryMappingBean> cat2Attribute, SolrCache cache) throws Exception {
		NamedList facetFields = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
		if (facetFields == null) {
			return;
		}

		List<NamedList> lstCatAtt = new ArrayList<NamedList>();
		AttributeModel attModel = (AttributeModel) model;

		for (int i = 0; i < facetFields.size(); i++) {
			String name = facetFields.getName(i);
			if (name.contains("attr")) {
				NamedList attFacet = (NamedList) facetFields.getVal(i);
				if (attFacet.size() > 0) {
					Integer attributeId = SolrResponseHelper.convertAttributeFieldName2AttId(name);
					if (!cat2Attribute.containsKey(attributeId)) {
						continue;
					}
					AttributeCategoryMappingBean mapper = cat2Attribute.get(attributeId);

					NamedList bean = new SimpleOrderedMap();
					bean.add("attId", attributeId);
					bean.add("attName", mapper.getAttributeName());
					bean.add("unitName", mapper.getUnitName());
					bean.add("typeValue", mapper.getAttributeType());
					List<NamedList> listValueBean = new ArrayList<NamedList>();
					Map<String, String> value2joinedStr = new HashMap<String, String>();
					for (int j = 0; j < attFacet.size(); j++) {
						String value = attFacet.getName(j);
						NamedList valueBean = new SimpleOrderedMap();
						valueBean.add("value", value);
						valueBean.add("numFound", attFacet.getVal(j));

						//TODO:MinhVV2 - only attribute type = 4 that we can use displayValue 
						if (mapper.getAttributeType() == 4) {
							Double doubleVal = null;
							try {
								doubleVal = Double.valueOf(value);
							} catch (NumberFormatException ex) {
								doubleVal = null;
							}
							if (doubleVal != null) {
								value2joinedStr.put(value,
										attributeId.toString() + "_" + DoubleUtils.formatDouble(doubleVal));
							}
						}
						listValueBean.add(valueBean);
					}

					if (value2joinedStr.size() > 0) {
						Map<String, String> transformed2DisplayValue = null;
						transformed2DisplayValue = (Map) cache.get(value2joinedStr);
						if (transformed2DisplayValue == null) {
							transformed2DisplayValue = attModel.getDisplayAttribute(value2joinedStr);
							cache.put(value2joinedStr, transformed2DisplayValue);
						}
						for (NamedList valBean : listValueBean) {
							String normalVal = (String) valBean.get("value");
							String displayVal = transformed2DisplayValue.get(normalVal);
							if (Strings.isNullOrEmpty(displayVal)) {
								displayVal = DoubleUtils.transform(Double.valueOf(normalVal), mapper.getUnitName());
							}
							valBean.add("displayValue", displayVal);
						}
					}
					bean.add("lvalue", listValueBean);
					lstCatAtt.add(bean);
				}
			}
		}

		rsp.add(GetFilterReturnField.LIST_ATT, lstCatAtt);

	}

	private String transform(Double doubleVal, String unit) {
		Integer accuracy = Integer.valueOf(System.getProperty("getfilter.attribute.doubleaccuracy", "3"));
		String strAccuracy = StringUtils.repeat("#", accuracy);
		DecimalFormat format = new DecimalFormat("0." + strAccuracy);
		String result = format.format(doubleVal);
		return (result + " " + unit.trim());
	}

}
