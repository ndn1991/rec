/**
 * 
 */
package com.adr.bigdata.search.handler;

import org.apache.solr.common.util.NamedList;

import com.adr.bigdata.search.handler.query.frontend.ComboCreationQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.FrontendResponseExecutor;

/**
 * @author minhvv2
 *
 */
public class ComboCreationRequestHandler extends AdrSearchBaseHandler {
	public void init(NamedList args) {
		super.init(args);
		builder = new ComboCreationQueryBuilder();
		responseExecutor = new FrontendResponseExecutor();
	}
}
