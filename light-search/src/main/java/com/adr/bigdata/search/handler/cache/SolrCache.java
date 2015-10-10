package com.adr.bigdata.search.handler.cache;

import java.util.ArrayList;
import java.util.List;

import net.minidev.json.parser.ParseException;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.request.LocalSolrQueryRequest;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.indexing.db.sql.beans.MerchantBean;
import com.nhb.common.Loggable;
import com.nhb.common.data.PuObject;

@SuppressWarnings("unsued")
@Deprecated
public class SolrCache implements Loggable {
	private SolrQueryResponse response = new SolrQueryResponse();
	private ModifiableSolrParams params = new ModifiableSolrParams();
	private SolrCore cacheCore;

	private List<BrandBean> brandBeans = new ArrayList<BrandBean>();
	private AttributeCategoryMappingBean attributeCategoryMappingBean;
	private List<CategoryBean> categoryBeans = new ArrayList<CategoryBean>();
	private String returnCategoryTree;
	private List<MerchantBean> merchantBeans = new ArrayList<MerchantBean>();

	public void setReq(SolrQueryRequest req) {
		cacheCore = req.getCore().getCoreDescriptor().getCoreContainer().getCore("cache");
	}

	public void addBrandKeys(List<Integer> brandIds) {
		for (Integer brandId : brandIds) {
			params.add("id", "brand_" + brandId);
		}
	}

	public void setCatAttFilterKey(int catId) {
		params.add("id", "cat_att_filter_" + catId);
	}

	public void addCatKeys(List<Integer> catIds) {
		for (Integer catId : catIds) {
			params.add("id", "cat_" + catId);
		}
	}

	public void setCategoryTreeKey(int catId) {
		params.add("id", "cat_tree_" + catId);
	}

	public void addMerchantKeys(List<Integer> mcIds) {
		for (Integer mcId : mcIds) {
			params.add("id", "mc_" + mcId);
		}
	}

	public void execute() {
		long start = System.currentTimeMillis();
		cacheCore.execute(cacheCore.getRequestHandler("get"), new LocalSolrQueryRequest(cacheCore, params), response);
		getLogger().info("time execute: {}", (System.currentTimeMillis() - start));
	}

	private void extract() {
		getLogger().info("rsp: {}", response.getValues().get("response"));
		NamedList rsp = (NamedList) response.getValues().get("response");
		getLogger().info("rsp: {}", rsp.get("docs"));
		SolrDocumentList docs = (SolrDocumentList) rsp.get("docs");
		for (SolrDocument doc : docs) {
			String id = (String) doc.getFieldValue("id");
			String value = (String) doc.getFieldValue("value");
			if (id.startsWith("brand_")) {
				try {
					PuObject pu = PuObject.fromJSON(value);
					BrandBean bean = new BrandBean();
					bean.setId(pu.getInteger("id"));
					bean.setImage(pu.getString("image"));
					bean.setName(pu.getString("name"));
					bean.setStatus(pu.getInteger("status"));
					brandBeans.add(bean);
				} catch (ParseException e) {
					getLogger().error("", e);
				}
			} else if (id.startsWith("cat_att_filter_")) {
				attributeCategoryMappingBean = new AttributeCategoryMappingBean();
				PuObject pu;
				try {
					pu = PuObject.fromJSON(value);
					attributeCategoryMappingBean.setAttributeId(pu.getInteger("att_id"));
					attributeCategoryMappingBean.setAttributeName(pu.getString("att_name"));
					attributeCategoryMappingBean.setAttributeType(pu.getInteger(""));
					attributeCategoryMappingBean.setBaseUnitId(pu.getInteger("base_unit_id"));
					attributeCategoryMappingBean.setCategoryId(pu.getInteger("category_id"));
					attributeCategoryMappingBean.setUnitName(pu.getString("unit_name"));
					// attributeCategoryMappingBean.setFilterSpan(filterSpan);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} else if (id.startsWith("cat_")) {

			} else if (id.startsWith("cat_tree_")) {

			} else if (id.startsWith("mc_")) {

			}
		}
	}
}
