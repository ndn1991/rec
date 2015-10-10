/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer;

import java.util.Map;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.search.SolrCache;

import com.adr.bigdata.search.handler.facetextractor.FacetConstant;
import com.adr.bigdata.search.handler.query.getfilter.bean.FirstClassSearchBean;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.AttributeWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.BrandWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.CatTreeWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.FeaturedWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.MerchantWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.PriceWriter;
import com.adr.bigdata.search.handler.query.getfilter.writer.strategy.WriterType;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;

/**
 * @author minhvv2
 *
 */
public class FirstClassSearchResponseWriter extends AbstractResponseWriter {
	public FirstClassSearchResponseWriter() {
		super();
		writersContainer.put(WriterType.BRAND_WRITER, new BrandWriter());
		writersContainer.put(WriterType.MERCHANT_WRITER, new MerchantWriter());
		writersContainer.put(WriterType.FEATURED_WRITER, new FeaturedWriter());
		writersContainer.put(WriterType.CATTREE_WRITER, new CatTreeWriter());
		writersContainer.put(WriterType.PRICE_WRITER, new PriceWriter());
		writersContainer.put(WriterType.ATT_WRITER, new AttributeWriter());
	}

	@Override
	public void write(SolrQueryResponse response, Object... data) {
		assert data.length >= 3;
		this.easeResponse(response);
		SolrCache cache = (SolrCache) data[0];
		CategoryTreeVO catTree = (CategoryTreeVO) data[1];
		FirstClassSearchBean query = (FirstClassSearchBean) data[2];
		Map attMapping = (Map) data[3];

		NamedList facetCounts = (NamedList) SolrQueryResponseHelper.erase(response, FacetConstant.FACET_COUNTS);
		NamedList statCounts = (NamedList) SolrQueryResponseHelper.erase(response, FacetConstant.STATS);

		FeaturedWriter featuredWriter = (FeaturedWriter) writersContainer.get(WriterType.FEATURED_WRITER);
		featuredWriter.writeComponent(response, facetCounts);

		BrandWriter brandWriter = (BrandWriter) writersContainer.get(WriterType.BRAND_WRITER);
		brandWriter.writeComponent(response, facetCounts, cache, query.getBrandIds());

		MerchantWriter merchantWriter = (MerchantWriter) writersContainer.get(WriterType.MERCHANT_WRITER);
		merchantWriter.writeComponent(response, facetCounts, cache);

		CatTreeWriter treeWriter = (CatTreeWriter) writersContainer.get(WriterType.CATTREE_WRITER);
		treeWriter.writeComponent(response, facetCounts, catTree);

		PriceWriter priceWriter = (PriceWriter) writersContainer.get(WriterType.PRICE_WRITER);
		priceWriter.writeComponent(response, statCounts);

		AttributeWriter attWriter = (AttributeWriter) writersContainer.get(WriterType.ATT_WRITER);
		attWriter.writeComponent(response, facetCounts, query.getCatId(), attMapping, cache);

	}

}
