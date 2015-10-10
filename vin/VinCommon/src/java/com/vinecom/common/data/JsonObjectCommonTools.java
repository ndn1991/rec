package com.vinecom.common.data;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.util.Map;

/**
 * Created by ndn on 7/21/2015.
 */
class JsonObjectCommonTools {

	static CommonData fromJson(String json) {
		Object o = JSONValue.parse(json);
		if (o instanceof JSONObject) {
			return fromJSONObject((JSONObject) o);
		} else if (o instanceof JSONArray) {
			return fromJSONArray((JSONArray) o);
		} else {
			throw new IllegalArgumentException(
					"The only JSONObject and JSONArray is accepted");
		}
	}

	private static CommonObject fromJSONObject(JSONObject obj) {
		CommonObject commonObject = new CommonObject();
		for (Map.Entry<String, Object> e : obj.entrySet()) {
			if (e.getValue() instanceof JSONObject) {
				commonObject.setCommonData(e.getKey(),
						fromJSONObject((JSONObject) e.getValue()));
			} else if (e.getValue() instanceof JSONArray) {
				commonObject.setCommonData(e.getKey(),
						fromJSONArray((JSONArray) e.getValue()));
			} else {
				commonObject.put(e.getKey(), e.getValue());
			}
		}
		return commonObject;
	}

	private static CommonArray fromJSONArray(JSONArray arr) {
		CommonArray commonArray = new CommonArray();
		for (Object o : arr) {
			if (o instanceof JSONObject) {
				commonArray.add(fromJSONObject((JSONObject) o));
			} else if (o instanceof JSONArray) {
				commonArray.add(fromJSONArray((JSONArray) o));
			} else {
				commonArray.add(o);
			}
		}
		return commonArray;
	}

	static String toJsonString(CommonData data) {
		if (data instanceof CommonObject) {
			return toJsonObject((CommonObject) data).toJSONString();
		} else if (data instanceof CommonArray) {
			return toJSONArray((CommonArray) data).toJSONString();
		}
		return "[" + data.toString() + "]";
	}

	private static JSONObject toJsonObject(CommonObject object) {
		JSONObject jsonObject = new JSONObject();
		for (Map.Entry<String, Object> e : object.entrySet()) {
			String key = e.getKey();
			Object value = e.getValue();

			if (value instanceof CommonObject) {
				jsonObject.put(key, toJsonObject((CommonObject) value));
			} else if (value instanceof CommonArray) {
				jsonObject.put(key, toJSONArray((CommonArray) value));
			} else {
				jsonObject.put(key, value);
			}
		}
		return jsonObject;
	}

	private static JSONArray toJSONArray(CommonArray arr) {
		JSONArray jsonArray = new JSONArray();
		for (Object o : arr) {
			if (o instanceof CommonObject) {
				jsonArray.add(toJsonObject((CommonObject) o));
			} else if (o instanceof CommonArray) {
				jsonArray.add(toJSONArray((CommonArray) o));
			} else {
				jsonArray.add(o);
			}
		}
		return jsonArray;
	}

	public static void main(String[] args) {
		System.out.println("xxxx");
	}
}
