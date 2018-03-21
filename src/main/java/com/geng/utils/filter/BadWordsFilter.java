package com.geng.utils.filter;



import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Author: shushenglin
 * Date:   16/9/23 20:33
 */
public class BadWordsFilter {
	private static final Logger logger = LoggerFactory.getLogger(BadWordsFilter.class);
	private static final String BAD_WORD_DEFAULT_REPLACEMENT = "**";
	private File badWordsFile;
	private long fileLastModified;
	private WordNode wordTree;

	enum MatchType{
		MIN_MATCH,
		MAX_MATCH
	}

	public BadWordsFilter(String badWordsFilePath) {
		this.badWordsFile = new File(badWordsFilePath);
		init();
	}

	public BadWordsFilter(File badWordsFile) {
		this.badWordsFile = badWordsFile;
		init();
	}

	public void init(){
		List<String> words = readFile(badWordsFile);
		if(words != null && words.size() > 0){
			if (words.size() == 1){
				// 旧版本的只有一行数据,通过","分隔的格式支持
				String[] wordArr = StringUtils.split(words.get(0), ",");
				List<String> wordList= new ArrayList<>(wordArr.length);
				Collections.addAll(wordList, wordArr);
				this.wordTree = addWordsToTree(wordList);
			}else {
				this.wordTree = addWordsToTree(words);
			}
		}
	}

	public boolean reload(){
		if (badWordsFile.lastModified() > fileLastModified){
			this.init();
			return true;
		}
		return false;
	}

	/**
	 * 判断是否包含敏感词
	 * @param input
	 * @return
	 */
	public boolean containsBadWords(String input){
		if(StringUtils.isBlank(input)){
			return false;
		}
		if(wordTree == null || wordTree.isEmpty()){
			throw new IllegalStateException("Bad words filter init fail");
		}
		for (int i = 0; i < input.length(); ++i) {
			int matchLen = getBadWordLength(input,i, MatchType.MIN_MATCH);
			if(matchLen > 0){
				return true;
			}
		}
		return false;
	}

	/**
	 * 替换文本中包含的敏感词,每个敏感词使用**替换
	 * @param input 输入文本
	 * @return 替换后的文本
	 */
	public String replaceBadWords(String input){
		return replaceBadWords(input,BAD_WORD_DEFAULT_REPLACEMENT);
	}

	public String replaceBadWords(String input,String replacement){
		if(StringUtils.isBlank(input)) {
			return input;
		}
		if(wordTree == null || wordTree.isEmpty()){
			logger.warn("Bad words tree is empty, may be the file {} not exists", badWordsFile);
			return input;
		}
		try {
			StringBuilder stringBuilder = new StringBuilder(input.length());
			for (int i = 0; i < input.length(); ) {
				int matchLen = getBadWordLength(input, i, MatchType.MAX_MATCH);
				if (matchLen > 0) {
					stringBuilder.append(replacement);
					i += matchLen;
				} else {
					stringBuilder.append(input.charAt(i));
					++i;
				}
			}
			return stringBuilder.toString();
		}catch (Throwable t){
			logger.error("replace bad words error for {}", input,t);
			return input;
		}
	}

	private int getBadWordLength(String input, int startIndex, MatchType matchType){
		int matchLength = 0;
		boolean matchFlag = false;
		WordNode node = wordTree;
		for (int i = startIndex; i < input.length(); ++i) {
			char ch = input.charAt(i);
			node = node.getNode(ch);
			if (node == null){
				break;
			}
			++matchLength;
			if(node.isEOW()){
				matchFlag = true;
				if (matchType == MatchType.MIN_MATCH){
					break;
				}
			}
		}
		if(!matchFlag){
			matchLength = 0;
		}
		return matchLength;
	}

	private WordNode addWordsToTree(List<String> wordList) {
		WordNode rootNode = new WordNode(false);
		WordNode currNode;
		for (String wordOrg : wordList) {
			currNode = rootNode;
			String word = wordOrg.trim();
			if(StringUtils.isBlank(word)){
				continue;
			}
			for (int i = 0; i < word.length(); i++) {
				char keyChar = word.charAt(i);
				WordNode node = currNode.getNode(keyChar);
				if(node != null){
					currNode = node;
				}else{
					WordNode newNode = new WordNode(false);
					currNode.addChar(keyChar,newNode);
					currNode = newNode;
				}
				if (i == word.length() - 1){
					currNode.setEOW(true);
				}
			}
		}
		return rootNode;
	}

	private List<String> readFile(String filePath){
		File file = new File(filePath);
		return readFile(file);
	}

	private List<String> readFile(File file) {
		try {
			fileLastModified = file.lastModified();
			return FileUtils.readLines(file);
		} catch (IOException e) {
			logger.error("read file {} error", file.getName(), e);
			return null;
		}
	}


}
