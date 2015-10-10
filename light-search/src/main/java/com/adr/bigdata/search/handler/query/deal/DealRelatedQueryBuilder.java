package com.adr.bigdata.search.handler.query.deal;

import java.util.Arrays;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.adr.bigdata.search.handler.query.QueryBuilder;
import com.google.common.base.Joiner;
import com.nhb.common.Loggable;

public class DealRelatedQueryBuilder extends QueryBuilder implements Loggable {

	@Override
	public SolrParams buildSolrQuery(SolrQueryRequest request) {
		SolrParams params = request.getParams();
		String name = params.get(DealParams.RL_NAME);
//		Integer id = params.getInt(DealParams.RL_ID);
		
		if (name == null || name.trim().isEmpty()) {
			throw new IllegalArgumentException("name is null or empty");
		}

		ModifiableSolrParams result = new ModifiableSolrParams();
		result.set(CommonParams.Q, name);

		String[] sCityIds = params.getParams(DealParams.CITY_ID);
		if (sCityIds != null && sCityIds.length > 0) {
			String[] cityIds = new String[sCityIds.length];
			for (int i = 0; i < sCityIds.length; i++) {
				cityIds[i] = DealFields.CITY_IDS + ":" + Integer.parseInt(sCityIds[i].trim());
			}
			getLogger().debug("cityIds: " + Arrays.asList(cityIds));
			result.set(CommonParams.FQ, Joiner.on(" OR ").join(cityIds));
		} else {
			getLogger().debug("cityIds is null");
		}
		
		return result;
	}

}
