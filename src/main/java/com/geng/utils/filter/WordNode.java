package com.geng.utils.filter;

import java.util.HashMap;
import java.util.Map;

/**
 * Author: shushenglin
 * Date:   16/9/23 20:41
 */
public class WordNode {
	private Map<Character, WordNode> treeMap;
	private boolean isEOW = false;

	public WordNode(boolean isEOW) {
		this.isEOW = isEOW;
	}

	public void addChar(char ch, WordNode node){
		if (treeMap == null){
			treeMap = new HashMap<>();
		}
		this.treeMap.put(ch, node);
	}

	public Map<Character, WordNode> getTreeMap() {
		return treeMap;
	}

	public WordNode getNode(Character character) {
		if (treeMap == null || treeMap.isEmpty()) {
			return null;
		}
		return treeMap.get(character);
	}

	public boolean isEOW() {
		return isEOW;
	}

	public void setEOW(boolean EOW) {
		isEOW = EOW;
	}

	public boolean isEmpty() {
		if(treeMap == null || treeMap.size() == 0){
			return true;
		}
		return false;
	}
}
