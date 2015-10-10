package com.vinecom.recommend.storage.impl;

import com.vinecom.common.data.CommonData;
import com.vinecom.common.storage.SolrStorage;

/**
 * Created by ndn on 7/21/2015.
 */
public class ForItemSolrStorage extends SolrStorage {
    ForItemSolrStorage() {
        super();
    }

    @Override
    public void save(CommonData object) throws Exception {
        baseSave(object);
    }
}
