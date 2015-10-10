package com.adr.bigdata.search.handler.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class CollectionUtils {
	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> list2Map(List<V> list, String keyName) throws Exception {
		Map<K, V> result = new HashMap<K, V>();
		for (V item : list) {
			result.put((K) item.getClass().getMethod(keyName).invoke(item), item);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <K, V> Map<K, V> array2Map(V[] list, String keyName) throws Exception {
		Map<K, V> result = new HashMap<K, V>();
		for (V item : list) {
			result.put((K) item.getClass().getMethod(keyName).invoke(item), item);
		}
		return result;
	}

	public Collection<Integer> stringCollection2IntCollection(Collection<String> input) {
		Collection result = new ArrayList<>();
		if (input == null || input.isEmpty()) {
			return result;
		}

		Iterator<String> it = input.iterator();
		while (it.hasNext()) {
			result.add(Integer.valueOf(it.next()));
		}

		return result;
	}
}
