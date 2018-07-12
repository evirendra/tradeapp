package com.xxx;

import java.util.HashMap;

public class KeyCache {

	private static final String TOKEN = "token";
	public static final HashMap<String, String> data = new HashMap<>();
	public static final String API_KEY = "API_KEY";
	public static final String API_SECRET = "API_SECRET";
	public static final String ACCESS_TOKEN = "ACCESS_TOKEN";

	static {
		data.put(API_KEY, "7ff4ape1tn1i1ni5");
		data.put(API_SECRET, "rjgxz2ey6xc1qf3rw44lr51tjwe86czy");
	}

	public static void addKey(String key, String value) {
		data.put(key, value);
	}

	public static String getKey(String key) {
		return data.get(key);
	}

	public static String getAPIKey() {
		return data.get(API_KEY);
	}

	public static String getAPISecretKey() {
		return data.get(API_SECRET);
	}

	public static String getAccessTokenKey() {
		return data.get(ACCESS_TOKEN);
	}

	public static void addAccessTokenKey(String value) {
		data.put(ACCESS_TOKEN, value);
	}
	
	public static String getAuthorizationStr() {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(TOKEN).append(" ").append(KeyCache.getAPIKey())
		.append(":").append(KeyCache.getAccessTokenKey());
		return stringBuilder.toString();
	}
	
	public static String convertToString() {
		return data.toString();
	}
	
	public static boolean containAccessToken() {
		return data.containsKey(ACCESS_TOKEN);
	}

}
