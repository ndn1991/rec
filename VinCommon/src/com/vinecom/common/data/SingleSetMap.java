package com.vinecom.common.data;

import java.util.HashMap;

/**
 * Created by ndn on 7/21/2015.
 */
public class SingleSetMap extends HashMap<String, Object> {
    public SingleSetMap(Object value) {
        super();
        put("set", value);
    }
}
