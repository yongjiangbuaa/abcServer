package com.geng.utils.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalGameConfigCache {
	private static LocalGameConfigCache instance;
	private Map<String, HashMap<String, String>> gameConfigItemMap;
	private Map<String, ArrayList<HashMap<String, String>>> gameConfigGroupMap;
	public final static String ALL_ITEM_KEY = "all_items_in_file";

	public static LocalGameConfigCache getInstance() {
		if(instance == null) {
			instance = new LocalGameConfigCache();
		}
		return instance;
	}

	public void clear() {
		gameConfigGroupMap.clear();
		gameConfigItemMap.clear();
	}
	
	private LocalGameConfigCache() {
		gameConfigItemMap = new HashMap<>();
		gameConfigGroupMap = new HashMap<>();
	}
	
	public HashMap<String, String> getItem(String itemCacheKey) {
		return gameConfigItemMap.get(itemCacheKey);
	}
	
	public void putItemValue(String itemKey, Map<String, String> itemValue) {
		gameConfigItemMap.put(itemKey, (HashMap<String, String>)itemValue);
	}
	
	public ArrayList<HashMap<String, String>> getGroup(String groupCacheKey) {
		return gameConfigGroupMap.get(groupCacheKey);
	}
	
	public void putGroupValue(String groupKey, List<HashMap<String, String>> groupValue) {
		gameConfigGroupMap.put(groupKey, (ArrayList<HashMap<String, String>>) groupValue);
	}
	
	public Map<String, HashMap<String, String>> getItemList(List<String> itemCacheKeyList) {
		Map<String, HashMap<String, String>> elementList = new HashMap<String, HashMap<String, String>>();
		for(String itemCacheKey : itemCacheKeyList) {
			HashMap<String, String> itemMap = gameConfigItemMap.get(itemCacheKey);
			if(itemMap != null) {
				elementList.put(itemMap.get("id"), new HashMap<>(itemMap));
			}
		}
		return elementList;
	}
}
