package com.adr.bigdata.search.handler.cmservice;

import java.util.List;

import com.adr.bigdata.search.handler.cmservice.bean.CmPriceBean;
import com.nhb.common.Loggable;

@Deprecated
public interface PriceSyncStrategy extends Loggable {
	CmPriceBean[] execute(List<Integer> listProductItemId);
}
