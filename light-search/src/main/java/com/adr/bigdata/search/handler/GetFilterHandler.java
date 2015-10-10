package com.adr.bigdata.search.handler;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.LogManager;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.CloseHook;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.search.handler.db.sql.models.FilterModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.query.frontend.GetFilterQuery;
import com.adr.bigdata.search.handler.query.frontend.GetFilterQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.GetFilterResponseExecutor;
import com.adr.bigdata.search.handler.responsestrategy.GetFilterResponseStrategy;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;
import com.google.api.client.util.Strings;
import com.nhb.common.Loggable;
import com.nhb.common.utils.Initializer;

@SuppressWarnings({ "rawtypes" })
public class GetFilterHandler extends SearchHandler implements Loggable {
	private String CACHE_NAME;
	private String threadNamePattern = "hazelCast Worker #%d";

	protected GetFilterResponseStrategy responseExecutor;
	protected GetFilterQueryBuilder builder;
	protected FilterModel filterModel;

	private boolean isInitInvalidateCache;
	private ExecutorService pool;
	private static final int POOL_SIZE = 8;

	@Override
	public void init(NamedList args) {
		isInitInvalidateCache = false;
		HazelcastClientAdapter.getInstance();
		pool = new ThreadPoolExecutor(POOL_SIZE, Integer.MAX_VALUE, 6, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new ThreadFactory() {

					final AtomicInteger threadNumber = new AtomicInteger(1);

					@Override
					public Thread newThread(Runnable r) {
						return new Thread(r, String.format(threadNamePattern, threadNumber.getAndIncrement()));
					}
				});
		initFilterModel();
		builder = new GetFilterQueryBuilder();
		responseExecutor = new GetFilterResponseExecutor(filterModel, pool);
		CACHE_NAME = System.getProperty("solr.cache.name", "nativecache");
		super.init(args);
	}

	@Override
	public void handleRequest(SolrQueryRequest request, SolrQueryResponse response) {
		GetFilterQuery query = new GetFilterQuery(request);
		SolrCache cache = request.getSearcher().getCache(CACHE_NAME);
		if (!isInitInvalidateCache) {
			cacheInvalidate(cache);
		}

		if (query.isSearchByCat()) {
			Future<CategoryTreeVO> futureCatree = null;
			Future<SolrParams> futureParams = null;
			CategoryTreeVO catTree = null;

			Object cachedCatree = cache.get("catTree_" + query.getCateogryId());
			if (cachedCatree != null) {
				catTree = (CategoryTreeVO) cachedCatree;

			} else {
				final int catId = Integer.valueOf(query.getCateogryId());
				futureCatree = pool.submit(new Callable<CategoryTreeVO>() {

					@Override
					public CategoryTreeVO call() throws Exception {
						return filterModel.getCatTree(catId);
					}
				});
			}

			Object cachedAttribute = cache.get("catAtt_" + query.getCateogryId());
			if (cachedAttribute != null) {
				//cache hit
				request.setParams(builder.buildCachedAttributeQuery(request, response, query, cachedAttribute));
			} else {
				// get cattAttribute.....
				futureParams = pool.submit(new Callable<SolrParams>() {
					final GetFilterQueryBuilder _builder = builder;
					final SolrQueryRequest _request = request;
					final SolrQueryResponse _response = response;
					final GetFilterQuery _query = query;

					@Override
					public SolrParams call() throws Exception {
						return _builder.buildSolrQuery(_request, _response, _query);
					}
				});

			}

			if (futureCatree != null) {
				try {
					catTree = futureCatree.get();
					cache.put("catTree_" + query.getCateogryId(), catTree);
				} catch (InterruptedException | ExecutionException e) {
					log.error("error getting catTree...{}", e);
				}
			}
			if (futureParams != null) {
				try {
					request.setParams(futureParams.get());
					String catIdForAttributeFilter = Strings.isNullOrEmpty(query.getFilteredCategoryId()) ? query
							.getCateogryId() : query.getFilteredCategoryId();
					cache.put("catAtt_" + catIdForAttributeFilter, response.getValues().get("attributeCached"));
				} catch (InterruptedException | ExecutionException ex) {
					log.error("error building params...{}", ex.getMessage());
				}
			}

			super.handleRequest(request, response);

			responseExecutor.writeResponseWithCacheCategoryTree(response, request, query, catTree);

		} else {
			if (!Strings.isNullOrEmpty(query.getFilteredCategoryId())) {
				Object cachedAttribute = cache.get("catAtt_" + query.getFilteredCategoryId());
				if (cachedAttribute != null) {
					//cache hit
					request.setParams(builder.buildCachedAttributeQuery(request, response, query, cachedAttribute));

				} else {
					request.setParams(builder.buildSolrQuery(request, response, query));
					cache.put("catAtt_" + query.getFilteredCategoryId(), response.getValues().get("attributeCached"));
				}
			} else {
				request.setParams(builder.buildSolrQuery(request, response, query));
			}

			super.handleRequest(request, response);
			responseExecutor.makeUpResponseFromRequest(response, request, query);
		}

	}

	@Override
	public void inform(SolrCore core) {
		core.addCloseHook(new CloseHook() {
			@Override
			public void preClose(SolrCore sc) {
				log.info("closing solr core..." + this.getClass());

				LogManager.shutdown();
				pool.shutdown();
			}

			@Override
			public void postClose(SolrCore sc) {

			}
		});
		super.inform(core);
	}

	private void cacheInvalidate(SolrCache cache) {
		isInitInvalidateCache = true;
		final int delay = Integer.valueOf(System.getProperty("solr.cache.invalidate.time", "600")); // delay for 10 mins
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				cache.clear();
				// don't need to synchronized because other threads are allowed to read dirty state
			}
		}, 1, delay, TimeUnit.SECONDS);

	}

	private void initFilterModel() {
		if (System.getProperty("db.name", "notexist").equals("notexist")) {
			Initializer.bootstrap(this.getClass());
		}

		ModelFactory modelFactory = ModelFactory.newInstance();
		this.filterModel = modelFactory.getModel(FilterModel.class);
	}

}
