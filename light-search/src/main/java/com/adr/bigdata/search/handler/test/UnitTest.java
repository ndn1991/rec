package com.adr.bigdata.search.handler.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.adr.bigdata.search.handler.responsestrategy.bean.ComboSearchBean;

public class UnitTest {
	@Test
	public void testProductIds() {
		List<ComboSearchBean> lstBeans = new ArrayList<>();
		ComboSearchBean bean = new ComboSearchBean("586422");
		ComboSearchBean thatBean = new ComboSearchBean("586421");
		lstBeans.add(bean);
		lstBeans.add(thatBean);
		ComboSearchBean compareBean = new ComboSearchBean("586422");
		Map<String, List<ComboSearchBean>> processing = new HashMap<String, List<ComboSearchBean>>();
		processing.put("1", lstBeans);
		for (int i = 0; i < 5; i++) {
			for (ComboSearchBean item : processing.get("1")) {
				if (item.equals(compareBean)) {
					System.out.println(item.getProductItemId());
					break;
				}
			}
			System.out.println(i);
		}
		System.out.println(bean.equals(thatBean));
	}
}
