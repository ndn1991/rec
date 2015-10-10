/**
 * 
 */
package com.adr.bigdata.search.handler.query.getfilter.writer.strategy;

import org.apache.solr.common.util.NamedList;
import org.apache.solr.response.SolrQueryResponse;

import com.adr.bigdata.search.handler.db.sql.models.CategoryTreeModel;
import com.adr.bigdata.search.handler.db.sql.models.ModelFactory;
import com.adr.bigdata.search.handler.response.daos.GetFilterReturnField;
import com.adr.bigdata.search.handler.utils.SolrQueryResponseHelper;
import com.adr.bigdata.search.handler.vo.CategoryTreeVO;

/**
 * @author minhvv2
 *
 */
public class CatTreeWriter extends WriterStrategy {
	public CatTreeWriter() {
		this.model = ModelFactory.newInstance().getModel(CategoryTreeModel.class);
	}

	@Override
	public void writeComponent(SolrQueryResponse response, NamedList value, Object... data) {
		assert data.length >= 1;
		SolrQueryResponseHelper.write2Response(response, GetFilterReturnField.CATEGORY_TREE,
				((CategoryTreeVO) data[0]).toNamedList());
	}

}
