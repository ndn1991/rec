package com.adr.bigdata.updateprocessor.boost;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.update.AddUpdateCommand;
import org.apache.solr.update.processor.UpdateRequestProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Objects;

/**
* Don't be confusing with solr.RegrexpBoostProcessor.
* This is a customize version of that processor.
*
* @author Tong Hoang Anh
*/
public class RegexpBoostProcessor extends UpdateRequestProcessor {
	
	protected static final String INPUT_FIELD_PARAM = "inputField";
	protected static final String BOOST_FIELD_PARAM = "boostField";
	protected static final String BOOST_FILENAME_PARAM = "boostFilename";
	private static final String DEFAULT_INPUT_FIELDNAME = "url";
	private static final String DEFAULT_BOOST_FIELDNAME = "urlboost";

	private static final Logger log = LoggerFactory
			.getLogger(RegexpBoostProcessor.class);

	private boolean enabled = true;
	private String inputFieldname = DEFAULT_INPUT_FIELDNAME;
	private String boostFieldname = DEFAULT_BOOST_FIELDNAME;
	private String boostFilename;
	private List<BoostEntry> boostEntries = new ArrayList<BoostEntry>();
	private static final String BOOST_ENTRIES_CACHE_KEY = "boost-entries";

	RegexpBoostProcessor(SolrParams parameters, SolrQueryRequest request,
			SolrQueryResponse response, UpdateRequestProcessor nextProcessor,
			final Map<Object, Object> sharedObjectCache) {
		super(nextProcessor);
		this.initParameters(parameters);

		if (this.boostFilename == null) {
			log.warn("Null boost filename.  Disabling processor.");
			setEnabled(false);
		}

		if (!isEnabled()) {
			return;
		}

		try {
			synchronized (sharedObjectCache) {
				List<BoostEntry> cachedBoostEntries = (List<BoostEntry>) sharedObjectCache
						.get(BOOST_ENTRIES_CACHE_KEY);

				if (cachedBoostEntries == null) {
					log.debug("No pre-cached boost entry list found, initializing new");
					InputStream is = request.getCore().getResourceLoader()
							.openResource(boostFilename);
					cachedBoostEntries = initBoostEntries(is);
					sharedObjectCache.put(BOOST_ENTRIES_CACHE_KEY,
							cachedBoostEntries);
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Using cached boost entry list with "
								+ cachedBoostEntries.size() + " elements.");
					}
				}

				this.boostEntries = cachedBoostEntries;
			}
		} catch (IOException ioe) {
			log.warn("IOException while initializing boost entries from file "
					+ this.boostFilename, ioe);
		}
	}

	private void initParameters(SolrParams parameters) {
		if (parameters != null) {
			this.setEnabled(parameters.getBool("enabled", true));
			this.inputFieldname = parameters.get(INPUT_FIELD_PARAM,
					DEFAULT_INPUT_FIELDNAME);
			this.boostFieldname = parameters.get(BOOST_FIELD_PARAM,
					DEFAULT_BOOST_FIELDNAME);
			this.boostFilename = parameters.get(BOOST_FILENAME_PARAM);
		}
	}

	private List<BoostEntry> initBoostEntries(InputStream is)
			throws IOException {
		List<BoostEntry> newBoostEntries = new ArrayList<BoostEntry>();

		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				StandardCharsets.UTF_8));
		try {
			String line = null;
			while ((line = reader.readLine()) != null) {
				// Remove comments
				line = line.replaceAll("\\s+#.*$", "");
				line = line.replaceAll("^#.*$", "");

				// Skip empty lines or comment lines
				if (line.trim().length() == 0) {
					continue;
				}

				String[] fields = line.split("-");

				if (fields.length == 3) {
					String[] categories = fields[0].split(",");
					Set<Integer> setOfCategories = new HashSet<Integer>();
					for(String s: categories){
						setOfCategories.add(Integer.parseInt(s));
					}					
					String regexp = fields[1];
					String boost = fields[2];
					newBoostEntries.add(new BoostEntry(setOfCategories, Pattern.compile(regexp),
							Float.parseFloat(boost)));
					log.debug("Read regexp " + regexp + " with boost " + boost);
				} else {
					log.warn("Malformed config input line: " + line
							+ " (expected 2 fields, got " + fields.length
							+ " fields).  Skipping entry.");
					continue;
				}
			}
		} finally {
			IOUtils.closeQuietly(reader);
		}

		return newBoostEntries;
	}

	@Override
	public void processAdd(AddUpdateCommand command) throws IOException {
		if (isEnabled()) {
			processBoost(command);
		}
		super.processAdd(command);
	}

	public void processBoost(AddUpdateCommand command) {
		SolrInputDocument document = command.getSolrInputDocument();
		if (document.containsKey(inputFieldname)) {
			String value = (String) document.getFieldValue(inputFieldname);
			float boost = 1.0f;
			for (BoostEntry boostEntry : boostEntries) {
				Integer cateogy = (Integer) document.getFieldValue("category_id");
				if(boostEntry.getCategory().contains(cateogy)){
					if (boostEntry.getPattern().matcher(value).matches()) {
						if (log.isDebugEnabled()) {
							log.debug("Pattern match "
									+ boostEntry.getPattern().pattern() + " for "
									+ value);
						}
						boost = (boostEntry.getBoost() * 1000) * (boost * 1000)
								/ 1000000;
					}
				}
			}
			document.setField(boostFieldname, value, boost);
//			if (log.isDebugEnabled()) {
//				log.debug("Value " + boost + ", applied to field "
//						+ boostFieldname);
//			}
		}
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	private static class BoostEntry {
		
		private Set<Integer> category;
		private Pattern pattern;
		private float boost;

		public BoostEntry(Set<Integer> category, Pattern pattern, float d) {
			this.category = category;
			this.pattern = pattern;
			this.boost = d;
		}
		
		public Set<Integer> getCategory(){
			return this.category;
		}

		public Pattern getPattern() {
			return pattern;
		}

		public float getBoost() {
			return boost;
		}
		
		@Override
		public String toString(){
			return Objects.toStringHelper(BoostEntry.class)
					.add("category", Joiner.on(",").join(category))
					.add("pattern", pattern.toString())
					.add("boost", boost)
					.toString();
		}
	}
}
