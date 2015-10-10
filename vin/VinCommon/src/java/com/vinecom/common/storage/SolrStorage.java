package com.vinecom.common.storage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.solr.client.solrj.impl.ConcurrentUpdateSolrClient;
import org.apache.solr.common.SolrInputDocument;

import com.vinecom.common.data.CommonArray;
import com.vinecom.common.data.CommonData;
import com.vinecom.common.data.CommonObject;

/**
 * Created by ndn on 7/21/2015.
 */
public abstract class SolrStorage implements Storage {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3534300817263334437L;

	protected SolrStorage() {

	}

	public static final String CF_SOLR_PATH = "solrPath";
	public static final String CF_BUFFER_SIZE = "bufferSize";
	public static final String CF_NUM_THREAD = "numThread";
	public static final String CF_NAME = "name";
	public static final String CF_IS_COMMIT = "isCommit";
	public static final String CF_MAX_DOCS_PER_REQUEST = "numDocPerReq";

	private String name = "solr-storage";
	private String solrPath = null;
	private ConcurrentUpdateSolrClient client = null;
	private int bufferSize = 1000;
	private int numThread = 4;
	private boolean isCommit = false;
	private int numDocPerRequest = 10000;

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void init(CommonObject cf) {
		if (cf.containsKey(CF_NAME)) {
			this.name = cf.getString(CF_NAME);
		}
		if (cf.containsKey(CF_SOLR_PATH)) {
			this.setSolrPath(cf.getString(CF_SOLR_PATH));
		} else {
			throw new IllegalArgumentException("solrPath must be exits");
		}
		if (cf.containsKey(CF_BUFFER_SIZE)) {
			this.setBufferSize(cf.getInt(CF_BUFFER_SIZE));
		}
		if (cf.containsKey(CF_NUM_THREAD)) {
			this.setNumThread(cf.getInt(CF_NUM_THREAD));
		}
		if (cf.containsKey(CF_IS_COMMIT)) {
			this.setCommit(cf.getBool(CF_IS_COMMIT));
		}
		if (cf.containsKey(CF_MAX_DOCS_PER_REQUEST)) {
			this.setNumDocPerRequest(cf.getInt(CF_MAX_DOCS_PER_REQUEST));
		}

		this.client = new ConcurrentUpdateSolrClient(getSolrPath(),
				getBufferSize(), getNumThread());
	}

	protected void baseSave(CommonData data) throws Exception {
		if (data instanceof CommonObject) {
			save(Arrays.asList(baseCreateDoc((CommonObject) data)));
		} else if (data instanceof CommonArray) {
			List<SolrInputDocument> docs = new ArrayList<>();
			((CommonArray) data).forEach(o -> docs
					.add(baseCreateDoc((CommonObject) o)));
			save(docs);
		} else {
			throw new IllegalArgumentException("data is not accept");
		}
	}

	protected SolrInputDocument baseCreateDoc(CommonObject object) {
		SolrInputDocument doc = new SolrInputDocument();
		object.entrySet().forEach(e -> doc.addField(e.getKey(), e.getValue()));
		return doc;
	}

	protected void save(Collection<SolrInputDocument> docs) throws Exception {
		client.add(docs);
		if (isCommit()) {
			client.commit();
		}
	}

	public String getSolrPath() {
		return solrPath;
	}

	public void setSolrPath(String solrPath) {
		this.solrPath = solrPath;
	}

	public int getBufferSize() {
		return bufferSize;
	}

	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	public int getNumThread() {
		return numThread;
	}

	public void setNumThread(int numThread) {
		this.numThread = numThread;
	}

	public boolean isCommit() {
		return isCommit;
	}

	public void setCommit(boolean isCommit) {
		this.isCommit = isCommit;
	}

	public int getNumDocPerRequest() {
		return numDocPerRequest;
	}

	public void setNumDocPerRequest(int numDocPerRequest) {
		this.numDocPerRequest = numDocPerRequest;
	}
	
	
	public void close() {
		this.client.blockUntilFinished();
		this.client.close();
	}
}
