/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer;

import java.util.Map;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.search.handler.entity.FilterMessage;
import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.query.getfilter.bean.NormalSearchBean;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.AttributeWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.BrandWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.CatFacetWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.FeaturedWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.MerchantWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.PriceWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.WriterType;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;
import com.nhb.common.Loggable;

/**
 * @author minhvv2
 *
 */
public class NormalSearchResponseWriter extends AbstractResponseWriter implements Loggable {

	public NormalSearchResponseWriter() {
		super();
		writersContainer.put(WriterType.BRAND_WRITER, new BrandWriter());
		writersContainer.put(WriterType.MERCHANT_WRITER, new MerchantWriter());
		writersContainer.put(WriterType.FEATURED_WRITER, new FeaturedWriter());
		writersContainer.put(WriterType.CAT_WRITER, new CatFacetWriter());
		writersContainer.put(WriterType.PRICE_WRITER, new PriceWriter());
		writersContainer.put(WriterType.ATT_WRITER, new AttributeWriter());
	}

	@Override
	public void write(SolrQueryResponse response, Object... data) {
		assert data.length >= 3;
		this.easeResponse(response);

		SolrCache cache = (SolrCache) data[0];
		FilterMessage mess = (FilterMessage) data[1];

		NormalSearchBean query = (NormalSearchBean) mess.getData().get("query");
		Map attMapping = (Map) mess.getData().get("attMapping");

		NamedList facetCounts = (NamedList) SolrQueryResponseHelper.erase(response, FacetConstant.FACET_COUNTS);
		NamedList statCounts = (NamedList) SolrQueryResponseHelper.erase(response, FacetConstant.STATS);

		FeaturedWriter featuredWriter = (FeaturedWriter) writersContainer.get(WriterType.FEATURED_WRITER);
		featuredWriter.writeComponent(response, facetCounts);

		BrandWriter brandWriter = (BrandWriter) writersContainer.get(WriterType.BRAND_WRITER);
		brandWriter.writeComponent(response, facetCounts, cache, query.getBrandIds());

		MerchantWriter merchantWriter = (MerchantWriter) writersContainer.get(WriterType.MERCHANT_WRITER);
		merchantWriter.writeComponent(response, facetCounts, cache);

		CatFacetWriter catWriter = (CatFacetWriter) writersContainer.get(WriterType.CAT_WRITER);
		catWriter.writeComponent(response, facetCounts, cache);

		PriceWriter priceWriter = (PriceWriter) writersContainer.get(WriterType.PRICE_WRITER);
		priceWriter.writeComponent(response, statCounts);

		if (query.getCatId() > 0) {
			AttributeWriter attWriter = (AttributeWriter) writersContainer.get(WriterType.ATT_WRITER);
			attWriter.writeComponent(response, facetCounts, query.getCatId(), attMapping, cache);
		}

	}
}
