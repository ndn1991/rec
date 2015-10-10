package com.vinecom.common.data;

import java.io.Serializable;

/**
 * Created by ndn on 7/21/2015.
 */
public interface CommonData extends Serializable {
	public default String toJsonString() {
		return JsonObjectCommonTools.toJsonString(this);
	}

	public default CommonData fromJson(String json) {
		CommonData data = JsonObjectCommonTools.fromJson(json);
		copyFrom(data);
		return this;
	}

	public abstract void copyFrom(CommonData other);
}
