package com.adr.bigdata.search.handler.getfilterconsumer;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.solr.search.SolrCache;

import com.adr.bigdata.indexing.db.sql.beans.AttributeCategoryMappingBean;
import com.adr.bigdata.indexing.db.sql.beans.BrandBean;
import com.adr.bigdata.indexing.db.sql.beans.MerchantBean;
import com.adr.bigdata.search.handler.db.sql.models.AttributeModel;
import com.adr.bigdata.search.handler.db.sql.models.BrandModel;
import com.adr.bigdata.search.handler.db.sql.models.CategoryTreeModel;
import com.adr.bigdata.search.handler.db.sql.models.MerchantModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;
import com.lmax.disruptor.WorkHandler;
import com.nhb.common.Loggable;

public class CacheWorker implements WorkHandler<CacheEvent>, Loggable {

	@Override
	public void onEvent(CacheEvent event) throws Exception {
		ModelFactory modelFactory = ModelFactory.newInstance();
		CategoryTreeModel categoryModel = modelFactory.getModel(CategoryTreeModel.class);
		AttributeModel attributeModel = modelFactory.getModel(AttributeModel.class);
		BrandModel brandModel = modelFactory.getModel(BrandModel.class);
		MerchantModel merchantModel = modelFactory.getModel(MerchantModel.class);

		SolrCache cache = event.getCache();
		cache.clear();

		//Fillup all category cache
		Map<Integer, CategoryTreeVO> allTree = categoryModel.getFullTree();
		for (Entry<Integer, CategoryTreeVO> entry : allTree.entrySet()) {
			cache.put("catTree_" + entry.getKey(), entry.getValue());
		}

		//Fill up all attribute cache
		Map<Integer, Set<AttributeCategoryMappingBean>> allCat2Attrbute = attributeModel.getAllAttributeModel();
		for (Entry<Integer, Set<AttributeCategoryMappingBean>> entry : allCat2Attrbute.entrySet()) {
			cache.put("catAtt_" + entry.getKey(), entry.getValue());
		}

		//Fill up all brand cache
		Map<Integer, BrandBean> id2Brand = brandModel.getAllBrands();
		for (Entry<Integer, BrandBean> entry : id2Brand.entrySet()) {
			cache.put("brand_" + entry.getKey(), entry.getValue());
		}

		//Fill up all merchant cache
		Map<Integer, MerchantBean> idMerchant = merchantModel.getAllMerchant();
		for (Entry<Integer, MerchantBean> entry : idMerchant.entrySet()) {
			cache.put("merchant_" + entry.getKey(), entry.getValue());
		}
	}
}
