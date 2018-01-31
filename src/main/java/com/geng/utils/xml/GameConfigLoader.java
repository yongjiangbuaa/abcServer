package com.geng.utils.xml;

import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class GameConfigLoader {
    private static final Logger logger = LoggerFactory.getLogger(GameConfigLoader.class);

	public static void loadXml(String xmlFileName){
		logger.info("try to load xml file {}", xmlFileName);
		File file = new File( Constants.XML_RESOUCE_PATH + xmlFileName);
		if(!file.exists()){
			logger.error("xml file {} not exists", xmlFileName);
			return;
		}
		SAXReader reader = new SAXReader();
		String fileName = file.getName();
		try {
			Document document = reader.read(file);
			fileName = fileName.substring(0, fileName.length() - 4);
			putXMLToCache(fileName, document);
		} catch (Exception e) {
			logger.error("read xml fail " + xmlFileName, e);
		}
	}

	public static void loadXml() {
		File xmlPath = new File(Constants.XML_RESOUCE_PATH);
		if(xmlPath != null && xmlPath.isDirectory()) {
            String fileName = null;
			try {
				File[] xmlFiles = xmlPath.listFiles(new FileFilter() {
					@Override
					public boolean accept(File file) {
                        String fileName = file.getName();
						if(!fileName.matches("^.+\\.xml$") || fileName.equals("world.xml")) {
							return false;
						} else {
							return true;
						}
					}
				});
				SAXReader reader = new SAXReader();
				Document document;
				LocalGameConfigCache.getInstance().clear();
				for(File file : xmlFiles) {
					fileName = file.getName();
					document = reader.read(file);
					fileName = fileName.substring(0, fileName.length() - 4);
                    putXMLToCache(fileName, document);
				}
                logger.info("loading xml is ready");
			} catch (DocumentException e) {
                logger.info("loading xml is error", e);
				throw new RuntimeException(String.format("load %s error!", fileName));
			} catch(RuntimeException re) {
                logger.info("loading xml is error", re);
				throw new RuntimeException(re);
			} 
		}
	}

	/**
	 * 根据group
	 * @param fileName
	 * @param document
	 */
	private static void putXMLToCache(String fileName, Document document) {
		Element rootElement = document.getRootElement();
		List<?> groupElements = rootElement.elements("Group");
		if(groupElements.size() > 0) {
			putGroupToCache(fileName, groupElements);
		} else {
			List<?> elements = rootElement.elements();
			putItemsToCache(fileName, elements, null);
		}
	}
	
	/**
	 * 无group
	 * @param fileName
	 * @param elements
	 */
	private static void putItemsToCache(String fileName, List<?> elements, String groupName) {
		LocalGameConfigCache gameConfigCache = LocalGameConfigCache.getInstance();
		Iterator<?> iter = elements.iterator();
		List<HashMap<String, String>> allItemList = new ArrayList<HashMap<String, String>>();
		while(iter.hasNext()) {
			Element tmpElement = (Element)iter.next();
			ConfigMap itemValueMap = new ConfigMap();
			for(Iterator<?> it = tmpElement.attributeIterator(); it.hasNext();) {
				Attribute tmpAttr = (Attribute)it.next();
				itemValueMap.put(tmpAttr.getName(), tmpAttr.getValue());
			}
			gameConfigCache.putItemValue(GameConfigManager.getItemCacheKey(fileName, tmpElement.attributeValue("id")), itemValueMap);
			allItemList.add(itemValueMap);
		}
		gameConfigCache.putGroupValue(GameConfigManager.getItemCacheKey(fileName, LocalGameConfigCache.ALL_ITEM_KEY), allItemList);
	}
	
	/**
	 * 有group
	 * @param fileName
	 * @param groupElements
	 */
	private static void putGroupToCache(String fileName, List<?> groupElements) {
		LocalGameConfigCache gameConfigCache = LocalGameConfigCache.getInstance();
		List<HashMap<String, String>> allItemList = new ArrayList<HashMap<String, String>>();
		for(Object obj : groupElements) {
			Element groupElement = (Element)obj;
			String groupName = groupElement.attribute("id").getValue();
			List<HashMap<String, String>> groupItemList = new ArrayList<HashMap<String, String>>();
			for(Iterator<?> gIter = groupElement.elementIterator(); gIter.hasNext();) {
				Element tmpElement = (Element)gIter.next();
				ConfigMap itemValueMap = new ConfigMap();
				for(Iterator<?> it = tmpElement.attributeIterator(); it.hasNext();) {
					Attribute tmpAttr = (Attribute)it.next();
					itemValueMap.put(tmpAttr.getName(), tmpAttr.getValue());
				}
				groupItemList.add(itemValueMap);
				allItemList.add(itemValueMap);
				gameConfigCache.putItemValue(GameConfigManager.getItemCacheKey(fileName, tmpElement.attributeValue("id")), itemValueMap);
			}
			gameConfigCache.putGroupValue(GameConfigManager.getGroupCacheKey(fileName, groupName), groupItemList);
		}
		gameConfigCache.putGroupValue(GameConfigManager.getItemCacheKey(fileName, LocalGameConfigCache.ALL_ITEM_KEY), allItemList);
	}
}
