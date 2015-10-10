package com.adr.bigdata.search.handler.query.itemneigh;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.ndn.expression.tree.Tree;
import com.nhb.common.Loggable;

public class ItemNeighsQueryBuilder extends QueryBuilder implements Loggable {

	private ConcurrentHashMap<String, Map<Tree, String>> ruleSet;

	public ItemNeighsQueryBuilder(ConcurrentHashMap<String, Map<Tree, String>> ruleSet) {
		this.ruleSet = ruleSet;
	}

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		ItemNeighsQuery query = new ItemNeighsQuery(request, ruleSet.get("rules"));
		try {
			return query.getQueryMap();
		} catch (Exception e) {
			getLogger().error("error: ", e);
			ModifiableSolrParams params = new ModifiableSolrParams();
			params.set(CommonParams.Q, "product_id:0");
			return params;
		}
	}

}
