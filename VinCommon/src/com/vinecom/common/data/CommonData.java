package com.vinecom.common.data;

import java.io.Serializable;

/**
 * Created by ndn on 7/21/2015.
 */
public abstract class CommonData implements Serializable {
    private static final long serialVersionUID = 4057279265602960760L;

    public String toJsonString() {
        return JsonObjectCommonTools.toJsonString(this);
    }

    public CommonData fromJson(String json) {
        CommonData data = JsonObjectCommonTools.fromJson(json);
        copyFrom(data);
        return this;
    }

    public abstract void copyFrom(CommonData other);
}
