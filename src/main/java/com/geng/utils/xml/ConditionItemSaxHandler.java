/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年11月12日 下午2:55:33
 */
package com.geng.utils.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.*;
import java.util.Map.Entry;

/**
 * 根据条件读取
 */
public class ConditionItemSaxHandler extends DefaultHandler {
	private List<HashMap<String, String>> groupMap;
	private Map<String, String> conditionMap;
	
	public ConditionItemSaxHandler(Map<String, String> conditionMap) {
		groupMap = new ArrayList<>();
		this.conditionMap = conditionMap;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(Constants.XML_ELEMENT_NAME.equalsIgnoreCase(qName)) {
			Iterator<Entry<String, String>> iter = conditionMap.entrySet().iterator();
			boolean flag = true;
			while(iter.hasNext()) {
				Entry<String, String> entry = iter.next();
				if(!entry.getValue().equalsIgnoreCase(attributes.getValue(entry.getKey()))) {
					flag = false;
					break;
				}
			}
			if(flag) {
				HashMap<String, String> itemMap = new HashMap<String, String>();
				for(int i=0, length=attributes.getLength();i<length; i++) {
					itemMap.put(attributes.getLocalName(i), attributes.getValue(i).trim());
				}
				groupMap.add(itemMap);
			}
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
	}

	public List<HashMap<String, String>> getItems() {
		return groupMap;
	}
}
