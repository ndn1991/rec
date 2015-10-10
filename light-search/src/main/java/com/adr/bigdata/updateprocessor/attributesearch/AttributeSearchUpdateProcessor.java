/**
 * 
 */
package com.adr.bigdata.updateprocessor.attributesearch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.api.client.util.Strings;

/**
 * @author minhvv2
 *
 */
public class AttributeSearchUpdateProcessor extends UpdateRequestProcessor {
	private static final Logger log = LoggerFactory.getLogger(AttributeSearchUpdateProcessor.class);
	private boolean enabled = true;

	private Map<Integer, Map<Integer, CategoryAttributeConfig>> config;
	private static final String ATT_SEARCH_CACHE_KEY = "attr-search";

	private String configFileName = "attribute_search.json";
	private static final String CAT_ID = "catId";
	private static final String ATTRIBUTES = "attributes";
	private static final String ATT_NAME = "name";
	private static final String ATT_WEIGHT = "weight";
	private static final String ATT_ID = "id";
	private static final String CONFIG_FILE_PARAM = "attSearchFileName";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	AttributeSearchUpdateProcessor(SolrParams parameters, SolrQueryRequest request, SolrQueryResponse response,
			UpdateRequestProcessor nextProcessor, final Map<Object, Object> sharedObjectCache) {
		super(nextProcessor);
		this.initParameters(parameters);

		if (this.configFileName == null) {
			log.warn("Null attribute_search.json filename.  Disabling processor.");
			setEnabled(false);
		}

		if (!isEnabled()) {
			return;
		}
		try {
			synchronized (sharedObjectCache) {
				Map<Integer, Map<Integer, CategoryAttributeConfig>> cachedConfig = (Map) sharedObjectCache
						.get(ATT_SEARCH_CACHE_KEY);
				if (cachedConfig == null) {
					log.debug("No pre-cached att search list found, initializing new");
					InputStream is = request.getCore().getResourceLoader().openResource(configFileName);
					cachedConfig = loadConfig(is);
					sharedObjectCache.put(ATT_SEARCH_CACHE_KEY, cachedConfig);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Using cached att search list with " + cachedConfig.size() + " elements.");
					}
				}

				this.config = cachedConfig;
			}
		} catch (IOException ioe) {
			log.warn("IOException while initializing boost entries from file " + this.configFileName, ioe);
		}
	}

	private void initParameters(SolrParams parameters) {
		if (parameters != null) {
			this.setEnabled(parameters.getBool("enabled", true));
			this.configFileName = parameters.get(CONFIG_FILE_PARAM, "attribute_search.json");
		}
	}

	@Override
	public void processAdd(AddUpdateCommand command) throws IOException {
		if (isEnabled()) {
			processIndexAttribute(command);
		}
		super.processAdd(command);
	}

	public void processIndexAttribute(AddUpdateCommand command) {
		SolrInputDocument document = command.getSolrInputDocument();
		Collection<Object> oCatpath = document.getFieldValues("category_path");
		Collection<Integer> catPath = new ArrayList<Integer>();
		oCatpath.forEach(o -> catPath.add(Integer.parseInt(o.toString())));
		Set<CategoryAttributeConfig> searchedAttributes = new HashSet<CategoryAttributeConfig>();
		for (int catId : catPath) {
			if (config.containsKey(catId)) {
				Map<Integer, CategoryAttributeConfig> atts = config.get(catId);
				searchedAttributes.addAll(atts.values());
			}
		}

		if (!searchedAttributes.isEmpty()) {
			log.debug("searchedAttributes: {}", searchedAttributes);
			List<String> values = new ArrayList<String>();
			float weight = 1;
			for (CategoryAttributeConfig attConfig : searchedAttributes) {
				String attributeValue = (String) document.getFieldValue("attr_" + attConfig.getAttributeId() + "_txt");

				if (!Strings.isNullOrEmpty(attributeValue)) {
					values.add(attConfig.getAttributeName() + attributeValue);
					weight = attConfig.getWeight();
				}
			}

			if (!values.isEmpty()) {
				document.setField("attribute_search", values, weight);
			}
		}
	}

	private Map<Integer, Map<Integer, CategoryAttributeConfig>> loadConfig(InputStream is)
			throws UnsupportedEncodingException {
		Map<Integer, Map<Integer, CategoryAttributeConfig>> config = new HashMap<Integer, Map<Integer, CategoryAttributeConfig>>();
		try {
			JSONArray arr = ConfigReader.getJSONArrayConfigFromInputStream(is);
			for (Object o : arr) {
				loadConfig((JSONObject) o, config);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(is);
		}
		return config;
	}

	private void loadConfig(JSONObject o, Map<Integer, Map<Integer, CategoryAttributeConfig>> config) {
		int catId = (int) o.get(CAT_ID);
		JSONArray attributes = (JSONArray) o.get(ATTRIBUTES);
		for (Object obj : attributes) {
			JSONObject attribute = (JSONObject) obj;
			int attId = (int) attribute.get(ATT_ID);
			String attName = (String) attribute.get(ATT_NAME);
			float attWeight = attribute.getAsNumber(ATT_WEIGHT).floatValue();
			CategoryAttributeConfig caConfig = new CategoryAttributeConfig(catId, attId, attName, attWeight);
			if (config.containsKey(catId)) {
				config.get(catId).put(attId, caConfig);
			} else {
				Map<Integer, CategoryAttributeConfig> lst = new HashMap<Integer, CategoryAttributeConfig>();
				lst.put(attId, caConfig);
				config.put(catId, lst);
			}
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

}
