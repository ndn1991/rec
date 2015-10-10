package com.adr.bigdata.search.handler.query.frontend;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.solr.common.params.ModifiableSolrParams;

import com.nhb.common.Loggable;
import com.nhb.common.utils.FileSystemUtils;

public class TrendingKeywordPreprocessor implements Loggable {

	private ConcurrentHashMap<String, List<String>> keyword2CatId = new ConcurrentHashMap<String, List<String>>();

	public TrendingKeywordPreprocessor() {
		init();
	}

	private int delay = Integer.valueOf(System.getProperty("solr.trending.keyword.delay", "300"));
	private String dictionaryPath;

	private void init() {
		dictionaryPath = FileSystemUtils.createPathFrom(FileSystemUtils.getBasePath(), "conf", "trending_keyword.txt");
		readDictionary(dictionaryPath);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				keyword2CatId.clear();
				readDictionary(dictionaryPath);
			}
		}, 1, delay, TimeUnit.SECONDS);

	}

	public void preProcessKeyword(ModifiableSolrParams solrParams, String keyword) {
		if (keyword.equals("*:*")) {
			return;
		}
		List<String> suggestCategories = this.getSuggestCategories(keyword);
		if (suggestCategories != null) {
			solrParams.set("bq", buildBQParams(suggestCategories));
			// no pf and qf when search trending keyword
			solrParams.remove("pf");
			solrParams.remove("qf");
		}
	}

	private String buildBQParams(List<String> catIds) {
		String ret = "";
		for (String id : catIds) {
			ret += " category_path:" + id + "^2";
		}
		return ret.trim();
	}

	private List<String> getSuggestCategories(String keyword) {
		if (keyword2CatId.isEmpty()) {
			return null;
		}

		for (String key : keyword2CatId.keySet()) {
			if (keyword.matches(key)) {
				return keyword2CatId.get(key);
			}
		}
		return null;
	}

	private void readDictionary(String filePath) {

		BufferedReader reader = null;
		try {
			String sCurrentLine;
			String cvsSplitBy = ",";
			reader = new BufferedReader(new FileReader(filePath));
			while ((sCurrentLine = reader.readLine()) != null) {
				if (!sCurrentLine.contains(",")) {
					continue;
				} else {
					String[] keywords = sCurrentLine.split(cvsSplitBy);
					if (keywords.length >= 2) {
						List<String> catIds = new ArrayList<String>();
						for (int i = 1; i < keywords.length; i++) {
							catIds.add(keywords[i]);
						}
						keyword2CatId.put(keywords[0], catIds);
					}
				}

			}
		} catch (Exception ex) {
			getLogger().error("error reading trending_keyword.properties file");
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					getLogger().error("error reading trending_keyword.properties file");
				}
			}
		}
	}

}
