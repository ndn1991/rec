/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer;

import java.util.HashMap;
import java.util.Map;

import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.WriterStrategy;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.WriterType;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;

/**
 * @author minhvv2
 *
 */
public abstract class AbstractResponseWriter {
	private static final String RESPONE = "response";

	protected Map<WriterType, WriterStrategy> writersContainer;

	public AbstractResponseWriter() {
		writersContainer = new HashMap<WriterType, WriterStrategy>();
	}

	public abstract void write(SolrQueryResponse response, Object... data);

	// remove the response component
	protected void easeResponse(SolrQueryResponse response) {
		SolrQueryResponseHelper.erase(response, RESPONE);
	}
}
