package com.adr.bigdata.search.handler.cmservice;

import java.io.IOException;
import java.util.List;

import com.adr.bigdata.search.handler.cmservice.bean.CmPriceBean;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.Key;
import com.google.common.base.Joiner;

@Deprecated
public class CmPriceService implements PriceSyncStrategy {
	private static final String CM_PRICE_SERVICE_URL = System
			.getProperty(
					"cm.service.url",
					"http://api.cm.adayroi.dev/bigdata/GetProductItemPriceAndDiscountByListProductItemId");
	private static final int CM_SERVICE_TIMEOUT = Integer.valueOf(System
			.getProperty("cm.service.timeout", "5000"));

	private CmSelpriceUrl url = new CmSelpriceUrl(CM_PRICE_SERVICE_URL);

	private final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
	private final JsonFactory JSON_FACTORY = new JacksonFactory();

	static class CmSelpriceUrl extends GenericUrl {

		public CmSelpriceUrl(String encodedUrl) {
			super(encodedUrl);
		}

		@Key("productitemids")
		private String productItemIds;
	}

	public CmPriceBean[] execute(List<Integer> listProductItemId) {

		HttpRequestFactory requestFactory = HTTP_TRANSPORT
				.createRequestFactory(new HttpRequestInitializer() {
					@Override
					public void initialize(HttpRequest request) {
						request.setParser(new JsonObjectParser(JSON_FACTORY));
						request.setConnectTimeout(CM_SERVICE_TIMEOUT);
					}
				});
		url.productItemIds = Joiner.on(",").join(listProductItemId.iterator());
		HttpRequest request;
		try {
			request = requestFactory.buildGetRequest(url);
			CmPriceBean[] result = request.execute().parseAs(
					CmPriceBean[].class);
			return result;
		} catch (IOException e) {
			getLogger().debug(
					"error when connect cm service...." + e.getMessage());
			return null;
		}

	}
}
