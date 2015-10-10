package com.vinecom.common.storage;

import java.io.Serializable;

import com.vinecom.common.Loggable;
import com.vinecom.common.data.CommonData;
import com.vinecom.common.data.CommonObject;

/**
 * Created by ndn on 7/21/2015.
 */
public interface Storage extends Loggable, Serializable {
    public String getName();
    public void init(CommonObject object);
    public void save(CommonData object) throws Exception;
}
