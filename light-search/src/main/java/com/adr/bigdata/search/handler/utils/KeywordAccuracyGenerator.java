package com.adr.bigdata.search.handler.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import com.nhb.common.utils.FileSystemUtils;

@Deprecated
public class KeywordAccuracyGenerator {
	private static KeywordAccuracyGenerator instance;
	private Set<String> setStopWord;
	private final static String DELIMITTER = " \t\f\n-";

	public static KeywordAccuracyGenerator getInstance() {
		if (instance == null) {
			instance = new KeywordAccuracyGenerator();
		}
		return instance;
	}

	private KeywordAccuracyGenerator() {
		setStopWord = new HashSet<String>();
		String stopwordConfigFilePath = FileSystemUtils.createPathFrom(FileSystemUtils.getBasePath(),
				System.getProperty("solr.stopword.file","conf/stopwords.txt"));
		try {
			List<String> lines = Files.readAllLines((new File(stopwordConfigFilePath)).toPath());
			for (String line : lines) {
				setStopWord.add(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * quangvh'function use to calculate the mm parameters (must match params)
	 */
	public String GetAccuracyFromKey(String keyword) {
		keyword = keyword.toLowerCase().trim();
		StringTokenizer st = new StringTokenizer(keyword, DELIMITTER);
		int n = 0;
		int m = st.countTokens();
		if (setStopWord != null) {
			for (int i = 0; i < m; i++) {
				if (!setStopWord.contains(st.nextToken().trim()))
					n++;
			}
		}

		// f(n) = k * (n / n+1) ^ n + b
		// k = 225
		// b = 0
		int k = 690, b = -206;
		double percent = k * Math.pow((double) n / (double) (n + 1), n) + b;

		percent = Math.min(percent, 100);
		percent = Math.max(percent, 50);

		return String.valueOf((int) percent) + "%";
	}
	
}
