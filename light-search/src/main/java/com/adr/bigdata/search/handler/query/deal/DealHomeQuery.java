package com.adr.bigdata.search.handler.query.deal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;

import com.google.common.base.Joiner;
import com.nhb.common.Loggable;

public class DealHomeQuery implements Loggable {
	private int limit = 3;
	private String sortyBy = null;
	private String order = null;
	private String[] catIds = null;
	private String[] cityIds = null;

	public DealHomeQuery(SolrQueryRequest req) {
		SolrParams params = req.getParams();
		this.limit = params.getInt(DealParams.LIMIT, 3);

		this.sortyBy = params.get(DealParams.SORT_BY);
		if (this.sortyBy != null && !this.sortyBy.trim().isEmpty()) {
			this.order = params.get(DealParams.ORDER);
			if (this.order == null || this.order.trim().isEmpty()) {
				throw new IllegalArgumentException("sortby is not null but order is null");
			}
			this.sortyBy = this.sortyBy.trim();
			this.order = this.order.trim();
		}

		String[] sCatIds = params.getParams(DealParams.LIST_CAT_ID);
		if (sCatIds == null || sCatIds.length <= 0) {
			getLogger().debug("catIds is null or empty");
		} else {
			this.catIds = new String[sCatIds.length];
			for (int i = 0; i < sCatIds.length; i++) {
				this.catIds[i] = DealFields.CAT_1 + ":" + Integer.parseInt(sCatIds[i]);
			}
		}

		String[] sCityIds = params.getParams(DealParams.CITY_ID);
		if (sCityIds != null && sCityIds.length > 0) {
			this.cityIds = new String[sCityIds.length];
			for (int i = 0; i < sCityIds.length; i++) {
				this.cityIds[i] = DealFields.CITY_IDS + ":" + Integer.parseInt(sCityIds[i].trim());
			}
			getLogger().debug("cityIds: " + Arrays.asList(this.cityIds));
		} else {
			getLogger().debug("cityIds is null");
		}
	}

	public SolrParams getQueryMap() {
		ModifiableSolrParams result = new ModifiableSolrParams();
		result.set(CommonParams.Q, "*:*");

		if (this.sortyBy != null && !this.sortyBy.isEmpty()) {
			result.set(CommonParams.SORT, this.sortyBy + " " + this.order);
		}

		List<String> filters = new ArrayList<String>();
		if (this.catIds != null && this.catIds.length > 0) {
			filters.add(Joiner.on(" OR ").join(this.catIds));
		}

		if (this.cityIds != null && this.cityIds.length > 0) {
			filters.add(Joiner.on(" OR ").join(this.cityIds));
		}

		if (!filters.isEmpty()) {
			String[] arrFilter = new String[filters.size()];
			filters.toArray(arrFilter);
			result.set(CommonParams.FQ, arrFilter);
		}

		result.set("group", true);
		result.set("group.field", DealFields.CAT_1);
		result.set("group.limit", limit);

		return result;
	}
}
