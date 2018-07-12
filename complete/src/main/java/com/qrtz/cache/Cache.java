package com.qrtz.cache;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class Cache {

	private static Set<String> subscriptionSet = new HashSet<>();
	private static ConcurrentHashMap<String, Number> cachedData = new ConcurrentHashMap<>();

	public static void add(String key, Number value) {
		cachedData.put(key, value);
	}

	public static Number get(String key) {
		Number value = cachedData.get(key);
		return value;
	}

	public static String toStringValue() {
		return cachedData.toString();
	}

	public static void subscribe(String instrumentSymbol) {
		subscriptionSet.add(instrumentSymbol);
	}

	public static void subscribe(List<String> instrumentSymbols) {
		subscriptionSet.addAll(instrumentSymbols);
	}

	public static Set<String> getSubscriptions() {
		return subscriptionSet;
	}

	public static ConcurrentHashMap<String, Number> getCachedData() {
		return cachedData;
	}
}
