/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.BrandWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.CatFacetWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.FeaturedWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.PriceWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.WriterType;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;

/**
 * @author minhvv2
 *
 */
public class BrandQueryResponseWriter extends AbstractResponseWriter {

	public BrandQueryResponseWriter() {
		super();
		writersContainer.put(WriterType.BRAND_WRITER, new BrandWriter());
		writersContainer.put(WriterType.FEATURED_WRITER, new FeaturedWriter());
		writersContainer.put(WriterType.CAT_WRITER, new CatFacetWriter());
		writersContainer.put(WriterType.PRICE_WRITER, new PriceWriter());
	}

	@Override
	public void write(SolrQueryResponse response, Object... data) {
		assert data.length >= 2;
		this.easeResponse(response);
		SolrCache cache = (SolrCache) data[0];
		int brandId = (int) data[1];
		BrandWriter brandWriter = (BrandWriter) writersContainer.get(WriterType.BRAND_WRITER);
		brandWriter.writeInfor(response, brandId, cache);// write the infor

		NamedList facetCount = (NamedList) SolrQueryResponseHelper.erase(response, FacetConstant.FACET_COUNTS);
		NamedList statCounts = (NamedList) SolrQueryResponseHelper.erase(response, FacetConstant.STATS);

		CatFacetWriter catWriter = (CatFacetWriter) writersContainer.get(WriterType.CAT_WRITER);
		catWriter.writeComponent(response, facetCount, cache);

		PriceWriter priceWriter = (PriceWriter) writersContainer.get(WriterType.PRICE_WRITER);
		priceWriter.writeComponent(response, statCounts);

		FeaturedWriter featureWriter = (FeaturedWriter) writersContainer.get(WriterType.FEATURED_WRITER);
		featureWriter.writeComponent(response, facetCount);

	}

}
