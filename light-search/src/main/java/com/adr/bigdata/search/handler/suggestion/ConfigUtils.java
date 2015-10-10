/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.adr.bigdata.search.handler.suggestion;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.solr.common.util.NamedList;

import com.adr.bigdata.search.handler.utils.StringUtils;

/**
 *
 * @author Tong Hoang Anh
 */
@SuppressWarnings("rawtypes")
public class ConfigUtils {

	public static Map<Integer, String> getCategory(NamedList configs) {
		if (configs == null) {
			return Collections.emptyMap();
		}
		Map<Integer, String> categories;
		String categoryFile = (String) configs.get("category");
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(categoryFile));
			categories = new HashMap<>();
			String line;
			while ((line = br.readLine()) != null) {
				String[] arr = line.split("###");
				if (arr.length == 3) {
					categories.put(StringUtils.atoi(arr[0]), arr[2]);
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return Collections.emptyMap();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return categories;
	}
}
