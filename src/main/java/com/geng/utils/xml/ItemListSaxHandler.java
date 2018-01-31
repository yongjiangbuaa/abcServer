/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年11月12日 下午2:55:33
 */
package com.geng.utils.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 读取多条XML
 */
public class ItemListSaxHandler extends DefaultHandler {
	private Map<String, HashMap<String, String>> itemListMap;
	private List<String> itemList;
	
	public ItemListSaxHandler(List<String> itemList) {
		itemListMap = new HashMap<>();
		this.itemList = itemList;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		String itemId = attributes.getValue("id");
		if(Constants.XML_ELEMENT_NAME.equalsIgnoreCase(qName) && itemList.contains(itemId)) {
			HashMap<String, String> itemMap = new HashMap<String, String>();
			for(int i=0, length=attributes.getLength();i<length; i++) {
				itemMap.put(attributes.getLocalName(i), attributes.getValue(i).trim());
			}
			itemListMap.put(itemId, itemMap);
		}
		if(itemListMap.size() == itemList.size()) {
			super.endDocument();
			throw new SAXException("find");
		}
	}
	
	public Map<String, HashMap<String, String>> getItems() {
		return itemListMap;
	}
}
