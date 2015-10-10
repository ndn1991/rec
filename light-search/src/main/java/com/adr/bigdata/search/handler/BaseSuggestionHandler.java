/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.ResponseWriterUtil;
import org.apache.solr.response.ResultContext;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.response.transform.DocTransformer;
import org.apache.solr.response.transform.TransformContext;
import org.apache.solr.search.DocList;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.indexing.db.sql.beans.CategoryBean;
import com.adr.bigdata.search.handler.db.sql.models.CategoryModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.query.frontend.SuggestionQueryBuilder;
import com.nhb.common.Loggable;

/**
 *
 * @author Tong Hoang Anh
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class BaseSuggestionHandler extends SearchHandler implements Loggable {

	private SuggestionQueryBuilder queryBuilder = null;
	private CategoryModel catModel = null;
	private String CACHE_NAME;

	//	private ExecutorService classifyExector;

	@Override
	public void init(NamedList args) {
		queryBuilder = new SuggestionQueryBuilder();
		this.catModel = ModelFactory.newInstance().getModel(CategoryModel.class);
		CACHE_NAME = System.getProperty("solr.cache.name", "nativecache");
		super.init(args);
	}

	@Override
	public void inform(SolrCore core) {
		//		getLogger().info("creating executor serving classication job ...");
		//		this.classifyExector = Executors.newFixedThreadPool(1,
		//				new ThreadFactoryBuilder().setNameFormat("classify-thread-%d").build());
		//		core.addCloseHook(new CloseHook() {
		//
		//			@Override
		//			public void preClose(SolrCore core) {
		//				if (classifyExector != null) {
		//					classifyExector.shutdown();
		//					try {
		//						classifyExector.awaitTermination(10, TimeUnit.SECONDS);
		//					} catch (InterruptedException ex) {
		//						ex.printStackTrace();
		//					}
		//				}
		//			}
		//
		//			@Override
		//			public void postClose(SolrCore core) {
		//
		//			}
		//		});
		super.inform(core);
	}

	@Override
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
		req.setParams(queryBuilder.buildSolrQuery(req));
		String keyword = req.getParams().get(CommonParams.Q);
		//		Future<List<ClassifyResult>> classifyFuture = classifyExector
		//				.submit(new ClassifyResultCallable(keyword));
		super.handleRequest(req, rsp);

		SolrDocumentList docList = null;
		Object suggestionResult = rsp.getValues().get("response");
		if (suggestionResult == null) {
			getLogger().debug("no suggestion");
		} else if (suggestionResult instanceof ResultContext) {
			ResultContext ctx = (ResultContext) suggestionResult;
			rsp.getValues().remove("response");
			try {
				docList = getSolrDocumentList(ctx.docs, rsp, req);
			} catch (Exception e) {
				getLogger().info("cannot get list of solr documents", e);
			}
		} else if (suggestionResult instanceof SolrDocumentList) {
			docList = (SolrDocumentList) suggestionResult;
		}

		List<String> listSuggestKeyword = getListSuggestion(docList);
		rsp.add("listSuggestKeyword", listSuggestKeyword);
		processFacet(keyword, rsp, req.getSearcher().getCache(CACHE_NAME));

		//		List<ClassifyResult> classifyResult = null;
		//		try {
		//			if (listSuggestKeyword.size() > 0) {
		//				classifyResult = classifyFuture.get();
		//			}
		//		} catch (Exception e) {
		//			getLogger().error("error when classifying", e);
		//		}
		//		if (classifyResult != null) {
		//			rsp.getValues().add(
		//					"listSuggestCategory",
		//					toNamedList(keyword, classifyResult)
		//					);
		//		}

	}

	private void processFacet(String keyword, SolrQueryResponse rsp, SolrCache cache) {
		NamedList facetCounts = (NamedList) rsp.getValues().get(FacetConstant.FACET_COUNTS);
		rsp.getValues().remove(FacetConstant.FACET_COUNTS);
		NamedList facetFields = (NamedList) facetCounts.get(FacetConstant.FACET_FIELDS);
		if (facetFields == null) {
			return;
		}
		NamedList categoryIds = (NamedList) facetFields.get(FacetConstant.CATEGORY_ID_FACET);
		if (categoryIds == null) {
			return;
		}
		Iterator<Map.Entry<String, Object>> it = categoryIds.iterator();
		Set<Integer> firstClassCatIs = new HashSet<Integer>();
		while (it.hasNext()) {
			Map.Entry<String, Object> next = it.next();
			if (firstClassCatIs.size() > 2) {
				//we only need the first 3 suggestion category
				break;
			}
			try {
				Integer catId = Integer.parseInt(next.getKey());
				String key = "catBean_" + next.getKey();
				Object cachedObject = cache.get(key);
				if (cachedObject == null) {
					cachedObject = catModel.getCategory(catId);
					cache.put(key, cachedObject);
				}
				CategoryBean catBean;
				if (cachedObject instanceof CategoryBean) {
					catBean = (CategoryBean) cachedObject;
					List<Integer> catPath = catBean.getPath();
					if (catPath != null) {
						int pathLength = catPath.size();
						if (pathLength > 0) {
							int firstClass = catPath.get(pathLength - 1);
							if (firstClass > 0) {
								firstClassCatIs.add(firstClass);
							} else if (pathLength > 1) {
								firstClassCatIs.add(catPath.get(pathLength - 2));
							}
						}
					}
				}
			} catch (NumberFormatException ex) {
				continue;
			}
		}

		ArrayList<NamedList> listSuggestCategory = new ArrayList<>();

		for (Integer firstClassId : firstClassCatIs) {
			String key = "catBean_" + firstClassId;
			Object cachedObject = cache.get(key);
			if (cachedObject == null) {
				cachedObject = catModel.getCategory(firstClassId);
				cache.put(key, cachedObject);
			}
			CategoryBean catBean = (CategoryBean) cachedObject;
			SimpleOrderedMap som = new SimpleOrderedMap();
			som.add("categoryName", catBean.getName());
			som.add("categoryId", catBean.getId());
			som.add("keyword", keyword);
			listSuggestCategory.add(som);
		}

		if (listSuggestCategory.size() > 0) {
			rsp.getValues().add("listSuggestCategory", listSuggestCategory);
		}
	}

	//	protected abstract List<ClassifyResult> classify(String keyword);

	private static SolrDocumentList getSolrDocumentList(DocList docs, SolrQueryResponse rsp, SolrQueryRequest req)
			throws IOException {
		SolrDocumentList returnList = new SolrDocumentList();
		TransformContext context = new TransformContext();
		context.req = req;

		DocTransformer transformer = rsp.getReturnFields().getTransformer();
		context.searcher = req.getSearcher();
		context.iterator = docs.iterator();
		if (transformer != null) {
			transformer.setContext(context);
		}
		int sz = docs.size();
		Set<String> fnames = rsp.getReturnFields().getLuceneFieldNames();
		for (int i = 0; i < sz; i++) {
			int id = context.iterator.nextDoc();
			Document doc = context.searcher.doc(id, fnames);
			SolrDocument sdoc = ResponseWriterUtil.toSolrDocument(doc, req.getSchema());
			if (transformer != null) {
				transformer.transform(sdoc, id);
			}
			returnList.add(sdoc);
		}
		if (transformer != null) {
			transformer.setContext(null);
		}

		return returnList;
	}

	private static List<String> getListSuggestion(SolrDocumentList docList) {
		if (docList == null) {
			return Collections.EMPTY_LIST;
		}
		List<String> listSuggestKeyword = new ArrayList<>();
		for (SolrDocument doc : docList) {
			IndexableField value = (IndexableField) doc.getFirstValue("product_item_name");
			listSuggestKeyword.add(value.stringValue());
		}

		return listSuggestKeyword;
	}

	protected class ClassifyResult {
		private final int categoryId;
		private final String category;
		private final double weight;

		public ClassifyResult(int categoryId, String category, double weight) {
			this.categoryId = categoryId;
			this.category = category;
			this.weight = weight;
		}

		public int getCategoryId() {
			return categoryId;
		}

		public String getCategory() {
			return category;
		}

		public double getWeight() {
			return weight;
		}

	}

	private static List<NamedList> toNamedList(String keyword, List<ClassifyResult> category) {
		if (category == null)
			return Collections.emptyList();
		ArrayList<NamedList> ret = new ArrayList<>();

		for (ClassifyResult c : category) {
			SimpleOrderedMap som = new SimpleOrderedMap();
			som.add("categoryName", c.getCategory());
			som.add("categoryId", c.getCategoryId());
			som.add("numFound", Integer.MIN_VALUE); // remove it
			som.add("keyword", keyword);
			som.add("weight", c.getWeight());
			ret.add(som);
		}
		return ret;
	}

	//	private class ClassifyResultCallable implements Callable<List<ClassifyResult>> {
	//
	//		private String keyword;
	//
	//		public ClassifyResultCallable(String keyword) {
	//			this.keyword = keyword;
	//		}
	//
	//		@Override
	//		public List<ClassifyResult> call() throws Exception {
	//			return classify(keyword);
	//		}
	//
	//	}
}
