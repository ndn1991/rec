package com.vinecom.common.data;

import java.util.HashMap;

/**
 * Created by ndn on 7/21/2015.
 */
public class SingleSetMap extends HashMap<String, Object> {

	private static final long serialVersionUID = 7292408234438753440L;

	public SingleSetMap(Object value) {
		super();
		put("set", value);
	}
}
