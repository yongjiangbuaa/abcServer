/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年9月17日 上午10:08:35
 */
package com.geng.utils.xml;

import com.geng.exception.IllegalXMLConfigException;
import com.geng.utils.properties.PropertyFileReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.util.*;
import java.util.Map.Entry;

/**
 * 游戏配置文件
 */
public class GameConfigManager {
    private static final Logger logger = LoggerFactory.getLogger(GameConfigManager.class);
    private String fileName;
    private File file;

    private static boolean debug;
    private static volatile boolean isRefreshCache;
    private static String xmlVersion;
    private static boolean isTestServer;

    static {
        debug = PropertyFileReader.getBooleanItem("debug", "false");
        isRefreshCache = false;
        isTestServer = PropertyFileReader.getItem("is_test_server", "0").equals("1");
    }

    public static void init() {
        GameConfigLoader.loadXml();
    }



	public GameConfigManager(String fileName) {
        this.fileName = fileName;
    }

    public boolean exists() {
        file = new File(new StringBuilder(Constants.XML_RESOUCE_PATH).append(fileName).append(".xml").toString());
        return file.exists();
    }

	// 是否需要实时读取xml
    public static boolean isReadFile() {
        return isTestServer ? false : (debug ? true : isRefreshCache);
    }
	




    /**
     * Add By shilei
     * 功能 通过两列作为查找Key定位一行
     * @return 
     */
    public static HashMap<String, String> getDoubleKey(String fileName, String key1, String key1Val, String key2, String key2Val){
    	HashMap<String, String> ret = null;
    	
    	List<HashMap<String, String>> configGroupList = getAll(fileName);
    	for(HashMap<String, String> member : configGroupList){
    		if (member.get(key1).equals(key1Val) && member.get(key2).equals(key2Val)) {
    			ret = member;
    			break;
			}
    	}
    	
    	return ret;
    }
    
    public static String getString(String fileName, String itemId, String key){
    	return getItem(fileName, itemId, false).get(key);
    }
    
    public static Long getLong(String fileName, String itemId, String key){
    	return getItem(fileName, itemId, false).getLong(key);
    }
    
    public static Integer getInt(String fileName, String itemId, String key){
		return getItem(fileName, itemId, false).getInt(key);
    }
    
    public static ConfigMap getItem(String fileName, String itemId){
    	return getItem(fileName, itemId, false);
    }

    public int getConfig(String itemId, String key){
    	return getItem(itemId, false).getInt(key);
    }
    /**
     * 取得一条xml配置信息
     *
     * @param itemId
     * @return
     */
    public ConfigMap getItem(String itemId) {
        return getItem(itemId, false);
    }

    public ConfigMap getItem(String itemId, boolean isChange) {
    	return getItem(fileName, itemId, isChange);
    }
    
    public static ConfigMap getItem(String fileName, String itemId, boolean isChange) {
    	ConfigMap configMap;
        if (!isReadFile()) {
			configMap = (ConfigMap) LocalGameConfigCache.getInstance().getItem(getItemCacheKey(fileName, itemId));
            if (!isChange) {
                configMap = configMap != null ? new ConfigMap(configMap) : new ConfigMap();
            }
        } else {
            ItemSaxHandler itemHandler = new ItemSaxHandler(itemId);
            configMap = (ConfigMap) itemHandler.getItem();
            execute(fileName, itemHandler, itemId);
        }
        if (configMap == null) {
            configMap = ConfigMap.EMPTY_MAP;
        }
        return configMap;
    }
    
    public List<HashMap<String, String>> getGroup(String groupName) {
    	return getGroup(fileName, groupName);
    }

    /**
     * 根据group id取得一组xml配置信息
     *
     * @param groupName
     * @return
     */
    public static List<HashMap<String, String>> getGroup(String fileName, String groupName) {
        List<HashMap<String, String>> configGroupList;
        if (!isReadFile()) {
			configGroupList = LocalGameConfigCache.getInstance().getGroup(getGroupCacheKey(fileName, groupName));
            configGroupList = newList(configGroupList);
        } else {
            GroupItemSaxHandler groupHandler = new GroupItemSaxHandler(groupName);
            execute(fileName, groupHandler, groupName);
            configGroupList = groupHandler.getGroup();
        }
        return configGroupList;
    }

    private static List<HashMap<String, String>> newList(List<HashMap<String, String>> configGroupList) {
        List<HashMap<String, String>> cloneList = new ArrayList<>();
        if (configGroupList != null) {
            for (Map<String, String> configMap : configGroupList) {
                cloneList.add(new HashMap<>(configMap));
            }
        }
        return cloneList;
    }
    
    public Map<String, HashMap<String, String>> getItems(List<String> itemIdList) {
    	return getItems(fileName, itemIdList);
    }

    /**
     * 同时获取多个ID的配置信息
     *
     * @return
     */
    public static Map<String, HashMap<String, String>> getItems(String fileName, List<String> itemIdList) {
        Map<String, HashMap<String, String>> itemsMap;
        if (!isReadFile()) {
            List<String> itemCacheKeyList = new ArrayList<>();
            for (String itemId : itemIdList) {
                itemCacheKeyList.add(getItemCacheKey(fileName, itemId));
            }
			itemsMap = LocalGameConfigCache.getInstance().getItemList(itemCacheKeyList);
        } else {
            ItemListSaxHandler itemListHandler = new ItemListSaxHandler(itemIdList);
            execute(fileName, itemListHandler, itemIdList.toString());
            itemsMap = itemListHandler.getItems();
        }
        return itemsMap;
    }
    
    public List<HashMap<String, String>> selectItems(Map<String, String> conditionMap) {
    	return selectItems(fileName, conditionMap);
    }

    public static List<HashMap<String, String>> selectItems(String fileName, Map<String, String> conditionMap) {
        List<HashMap<String, String>> retItems = new ArrayList<>();
        if (!isReadFile()) {
            List<HashMap<String, String>> allItems = getAll(fileName);
            for (HashMap<String, String> oneItem : allItems) {
                if (selectCondition(oneItem, conditionMap)) {
                    retItems.add(oneItem);
                }
            }
        } else {
            ConditionItemSaxHandler itemHandler = new ConditionItemSaxHandler(conditionMap);
            execute(fileName, itemHandler, conditionMap.toString());
            retItems = itemHandler.getItems();
        }
        return retItems;
    }

    private static boolean selectCondition(Map<String, String> oneItem, Map<String, String> conditionMap) {
        boolean meetCondition = true;
        for (Entry<String, String> conditionEntry : conditionMap.entrySet()) {
            if (!conditionEntry.getValue().equals(oneItem.get(conditionEntry.getKey()))) {
                meetCondition = false;
                break;
            }
        }
        return meetCondition;
    }

    public Map<String, HashMap<String, String>> getItems(String... idArr) {
        List<String> idList = new ArrayList<>();
        for (String str : idArr) {
            idList.add(str);
        }
        return getItems(idList);
    }

    public Map<String, HashMap<String, String>> getItems(Set<String> idSet) {
        List<String> idList = new ArrayList<>();
        if (idSet != null) {
            idList.addAll(idSet);
        }
        return getItems(idList);
    }

    public List<HashMap<String, String>> getItemList(String... idArr) {
        List<String> idList = new ArrayList<>();
        for (String str : idArr) {
            idList.add(str);
        }
        return getItemList(idList);
    }

    public List<HashMap<String, String>> getItemList(List<String> idList) {
        List<HashMap<String, String>> data = new ArrayList<>();
        if (idList == null || idList.size() == 0) {
            return data;
        }
        Map<String, HashMap<String, String>> mapData = getItems(idList);
        for (String idItem : idList) {
            data.add(mapData.get(idItem));
        }
        return data;
    }

    public List<HashMap<String, String>> getItemList(Set<String> idSet) {
        List<HashMap<String, String>> data = new ArrayList<>();
        if (idSet == null || idSet.size() == 0) {
            return data;
        }
        Map<String, HashMap<String, String>> mapData = getItems(idSet);
        for (String idItem : idSet) {
            data.add(mapData.get(idItem));
        }
        return data;
    }

    public Map<String, HashMap<String, String>> getGroupMap(String groupName) {
    	return getGroupMap(fileName, groupName);
    }

    public static Map<String, HashMap<String, String>> getGroupMap(String fileName, String groupName) {
        Map<String, HashMap<String, String>> data = new HashMap<>();
        List<HashMap<String, String>> groupElementList = getGroup(fileName, groupName);
        for (HashMap<String, String> itemMap : groupElementList) {
            data.put(itemMap.get("id"), itemMap);
        }
        return data;
    }



    /**
     * 获取XML中所有的配置信息
     */
    public List<HashMap<String, String>> getAll() {
    	return getAll(fileName);
    }

	/**
     * 获取XML中所有的配置信息
     */
    public static List<HashMap<String, String>> getAll(String fileName) {
        List<HashMap<String, String>> allConfigList;
        if (!isReadFile()) {
			allConfigList = LocalGameConfigCache.getInstance().getGroup(getGroupCacheKey(fileName, LocalGameConfigCache.ALL_ITEM_KEY));
            allConfigList = newList(allConfigList);
        } else {
            AllItemsSaxHandler allItems = new AllItemsSaxHandler();
            execute(fileName, allItems, LocalGameConfigCache.ALL_ITEM_KEY);
            allConfigList = allItems.getItemsList();
        }
        return allConfigList;
    }
    
    @SuppressWarnings("unused")
    private void execute(DefaultHandler saxHandler, String param) {
        execute(fileName, saxHandler, param);
    }

    @SuppressWarnings("unused")
	private static void execute(String fileName, DefaultHandler saxHandler, String param) {
        File  file = new File(new StringBuilder(Constants.XML_RESOUCE_PATH).append(fileName).append(".xml").toString());
        
        if(!file.exists()) {
        	logger.error("config file {} not exists!!!!", file.getName());
        	return;
        }
        
        InputStream inputStream = null;
        try {
            if (file != null) {
            	inputStream = new FileInputStream(file);
                SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
                saxParser.parse(inputStream, saxHandler);
            } else {
                throw new IOException("file and in all is null in reading xml");
            }
        } catch (FileNotFoundException e) {
//        	LoggerUtil.getInstance().recordException(e);
            throw new IllegalXMLConfigException(fileName + "," + param);
        } catch (SAXException e) {
            if (e.getMessage() == null || !e.getMessage().equals("find")) {
//                LoggerUtil.getInstance().recordException(e);
                throw new IllegalXMLConfigException(fileName + "," + param);
            }
        } catch (ParserConfigurationException e) {
//            LoggerUtil.getInstance().recordException(e);
            throw new IllegalXMLConfigException(fileName + "," + param);
        } catch (IOException e) {
//            LoggerUtil.getInstance().recordException(e);
            throw new IllegalXMLConfigException(fileName + "," + param);
        } finally {
            try {
            	if(inputStream != null){
            		inputStream.close();
            	}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String getGroupCacheKey(String fileName, String groupName) {
        return fileName + "-" + groupName;
    }

    public static String getItemCacheKey(String fileName, String itemId) {
        return fileName + "-" + itemId;
    }

}
