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

public class DealQuery implements Loggable {
	private String keyword = null;
	private String sortBy = null;
	private String order = null;
	private Integer catId = null;
	private Integer offset = null;
	private Integer limit = null;
	private String[] priceFilters = null;
	private String[] promotionFilters = null;
	private String[] desIds = null;
	private String[] catLeafIds = null;
	private String[] cityIds = null;

	public DealQuery(SolrQueryRequest req) {
		SolrParams params = req.getParams();
		this.keyword = params.get(DealParams.KEYWORD);
		if (this.keyword == null || this.keyword.trim().isEmpty()) {
			this.keyword = "*:*";
		}
		getLogger().debug("keyword: " + keyword);

		this.sortBy = params.get(DealParams.SORT_BY);
		if (this.sortBy == null || this.sortBy.trim().isEmpty()) {
			this.sortBy = null;
			this.order = null;
		} else {
			this.order = params.get(DealParams.ORDER);
			if (this.order == null || this.order.isEmpty()) {
				throw new IllegalArgumentException("sortby is not null but order is null");
			}
			this.sortBy = this.sortBy.trim();
			this.order = this.order.trim();
		}
		getLogger().debug("sortBy: " + sortBy);
		getLogger().debug("order: " + order);

		try {
			this.catId = params.getInt(DealParams.CAT_ID);
		} catch (Exception e) {
			getLogger().error("", e);
		}
		getLogger().debug("catId: " + catId);

		try {
			this.offset = params.getInt(DealParams.OFFSET, 0);
		} catch (Exception e) {
			getLogger().error("", e);
		}
		getLogger().debug("offset: " + offset);

		try {
			this.limit = params.getInt(DealParams.LIMIT, 18);
		} catch (Exception e) {
			getLogger().error("", e);
		}
		getLogger().debug("limit: " + limit);

		this.priceFilters = params.getParams(DealParams.PRICE_FILTER);
		if (this.priceFilters != null && this.priceFilters.length > 0) {
			for (int i = 0; i < this.priceFilters.length; i++) {
				this.priceFilters[i] = DealFields.SELL_PRICE + ":[" + this.priceFilters[i] + "]";
			}
			getLogger().debug("priceFilters: " + Arrays.asList(this.priceFilters));
		} else {
			getLogger().debug("priceFilters is null");
		}

		this.promotionFilters = params.getParams(DealParams.PROMOTION_FILTER);
		if (this.promotionFilters != null && this.promotionFilters.length > 0) {
			for (int i = 0; i < this.promotionFilters.length; i++) {
				this.promotionFilters[i] = DealFields.DISCOUNT_PERCENT + ":[" + this.promotionFilters[i] + "]";
			}
			getLogger().debug("promotionFilters: " + Arrays.asList(this.promotionFilters));
		} else {
			getLogger().debug("promotionFilters is null");
		}

		String[] sDesIds = params.getParams(DealParams.DES_ID);
		if (sDesIds != null && sDesIds.length > 0) {
			this.desIds = new String[sDesIds.length];
			for (int i = 0; i < sDesIds.length; i++) {
				this.desIds[i] = DealFields.DESTINATION_IDS + ":" + Integer.parseInt(sDesIds[i].trim());
			}
			getLogger().debug("desIds: " + Arrays.asList(this.desIds));
		} else {
			getLogger().debug("sDesIds is null");
		}

		String[] sCatLeafIds = params.getParams(DealParams.CAT_LEAFT_ID);
		if (sCatLeafIds != null && sCatLeafIds.length > 0) {
			this.catLeafIds = new String[sCatLeafIds.length];
			for (int i = 0; i < sCatLeafIds.length; i++) {
				this.catLeafIds[i] = DealFields.CAT_PATH + ":" + Integer.parseInt(sCatLeafIds[i].trim());
			}
			getLogger().debug("catLeafIds: " + Arrays.asList(this.catLeafIds));
		} else {
			getLogger().debug("catLeafIds is null");
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
		result.set(CommonParams.Q, this.keyword);
		if (this.offset != null) {
			result.set(CommonParams.START, String.valueOf(this.offset));
		}
		if (this.limit != null) {
			result.set(CommonParams.ROWS, String.valueOf(this.limit));
		}

		if (this.sortBy != null && this.order != null) {
			result.set(CommonParams.SORT, this.sortBy + " " + this.order);
		}

		// Calculate filter
		List<String> filters = new ArrayList<String>();
		if (this.catId != null) {
			filters.add(DealFields.CAT_PATH + ":" + this.catId);
		}

		if (this.priceFilters != null && this.priceFilters.length > 0) {
			filters.add("{!tag=price}(" + Joiner.on(" OR ").join(this.priceFilters) + ")");
		}

		if (this.promotionFilters != null && this.promotionFilters.length > 0) {
			filters.add(Joiner.on(" OR ").join(this.promotionFilters));
		}

		if (this.desIds != null && this.desIds.length > 0) {
			filters.add("{!tag=des}(" + Joiner.on(" OR ").join(this.desIds) + ")");
		}

		if (this.catLeafIds != null && this.catLeafIds.length > 0) {
			filters.add("{!tag=cat}(" + Joiner.on(" OR ").join(this.catLeafIds) + ")");
		}
		if (this.cityIds != null && this.cityIds.length > 0) {
			filters.add(Joiner.on(" OR ").join(this.cityIds));
		}

		String[] arrFilter = new String[filters.size()];
		filters.toArray(arrFilter);
		result.set(CommonParams.FQ, arrFilter);
		// End: Calculate filter

		result.set("facet", true);
		result.add("facet.field", "{!ex=cat}" + DealFields.CAT_FACET);
		result.add("facet.field", "{!ex=cat}" + DealFields.CAT_3_FACET);
		result.add("facet.field", "{!ex=des}" + DealFields.DESTINATION_FACETS);
		result.set("facet.mincount", 1);

		result.set("stats", true);
		result.add("stats.field", "{!ex=price,cat,des max=true}" + DealFields.SELL_PRICE);

		getLogger().debug("solrParams: " + result);
		return result;
	}
}
