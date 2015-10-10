package com.vinecom.rec.app.storage

import com.vinecom.common.storage.SolrStorage
import com.vinecom.common.data.CommonData
import collection.JavaConversions._
import com.vinecom.rec.app.ultil.Utils._
import com.vinecom.rec.app.vo.config.Storage

/**
 * @author ndn
 */
class RecommendSolrStorage(config: Storage) extends SolrStorage {
	super.init(config.toCommonObject())

	def save(x: CommonData): Unit = {
		baseSave(x)
	}
}