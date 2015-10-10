/**
 * 
 */
package com.adr.bigdata.updateprocessor.attributesearch;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;

/**
 * @author ndn
 *
 */
public class ConfigReader {
	public static JSONArray getJSONArrayConfigFromInputStream(InputStream is) throws FileNotFoundException,
			UnsupportedEncodingException {
		InputStreamReader reader = new InputStreamReader(is, "UTF-8");
		return (JSONArray) JSONValue.parse(reader);
	}
}
