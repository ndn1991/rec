package com.adr.bigdata.search.handler.response.daos;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.adr.bigdata.search.handler.db.sql.models.FilterModel;
import com.adr.bigdata.search.handler.responsestrategy.bean.FilterModelQueryBean;

public class HazelCastWorkSubmittor {

	private final FilterModel filterModel;
	private ExecutorService pool;

	public HazelCastWorkSubmittor(FilterModel filterModel, ExecutorService pool) {
		this.filterModel = filterModel;
		this.pool = pool;
	}

	public Future submitHazelJob(FilterModelQueryBean modelQuery, String beanType) {
		if (beanType.equals("brand")) {
			final Set<Integer> facetedIds = modelQuery.getFacetedBrandIds().keySet();
			return pool.submit(new Callable<Collection>() {

				@Override
				public Collection call() throws Exception {
					return filterModel.getFacetedBrands(facetedIds);
				}
			});
		} else if (beanType.equals("merchant")) {
			final Set<Integer> facetedIds = modelQuery.getFacetedMerchantIds().keySet();
			return pool.submit(new Callable<Collection>() {

				@Override
				public Collection call() throws Exception {
					return filterModel.getFacetedMerchants(facetedIds);
				}
			});
		} else if (beanType.equals("category")) {
			final Set<Integer> facetedIds = modelQuery.getFacetedCategoryIds().keySet();
			return pool.submit(new Callable<Collection>() {

				@Override
				public Collection call() throws Exception {
					return filterModel.getFacetCategories(facetedIds);
				}
			});
		} else {
			throw new IllegalArgumentException("beanType..." + beanType
					+ " is not supported for getting from hazelcast");
		}
	}

}
