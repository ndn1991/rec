package com.adr.bigdata.search.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.query.itemneigh.ItemNeighsQueryBuilder;
import com.adr.bigdata.search.handler.responsestrategy.ItemNeighsResponseExecutor;
import com.ndn.expression.ExpressionTreeBuilder;
import com.ndn.expression.Infix2PostfixConverter;
import com.ndn.expression.tree.Tree;

/**
 * 
 * @author ndn
 *
 */
public class ItemNeighsRequestHandler extends AdrSearchBaseHandler {
	private ExpressionTreeBuilder eBuilder = new ExpressionTreeBuilder();
	private Infix2PostfixConverter converter = new Infix2PostfixConverter();

	private ConcurrentHashMap<String, Map<Tree, String>> ruleSet = new ConcurrentHashMap<String, Map<Tree, String>>();

	private AtomicBoolean firstTime = new AtomicBoolean(true);
	private int delay = Integer.valueOf(System.getProperty("solr.trending.keyword.delay", "600"));

	@SuppressWarnings("rawtypes")
	@Override
	public void init(NamedList args) {
		super.init(args);
		responseExecutor = new ItemNeighsResponseExecutor();
		builder = new ItemNeighsQueryBuilder(ruleSet);
	}

	@Override
	public void handleRequest(SolrQueryRequest req, SolrQueryResponse rsp) {
		if (firstTime.get()) {
			firstTime.set(false);
			scheduleLoadRule(req);
		}

		super.handleRequest(req, rsp);
	}

	private void scheduleLoadRule(SolrQueryRequest req) {
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
		executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
					loadRules(req);
				} catch (IOException e) {
					getLogger().error("", e);
				}
			}
		}, 1, delay, TimeUnit.SECONDS);
	}

	private void loadRules(SolrQueryRequest req) throws IOException {
		InputStream is = req.getCore().getResourceLoader().openResource("recommend-rule.txt");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		Map<String, String> map = new HashMap<String, String>();
		while ((line = reader.readLine()) != null) {
			String[] ss = line.split("=>");
			if (ss.length > 1) {
				map.put(ss[0].trim(), ss[1].trim());
			}
		}
		getLogger().error("xxx: " + map);

		Map<Tree, String> mapRule = new HashMap<Tree, String>();
		int i = 0;
		for (String rule: map.keySet()) {
			mapRule.put(this.eBuilder.build(this.converter.convert(rule), i), map.get(rule));
			i++;
		}
		this.ruleSet.put("rules", mapRule);
	}
}
