package com.geng.utils.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 读取xml 中所有itemId
 */
public class AllItemsSaxHandler extends DefaultHandler {
	private List<HashMap<String, String>> itemsList;
	
	public AllItemsSaxHandler() {
		itemsList = new ArrayList<>();
	}
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(Constants.XML_ELEMENT_NAME.equalsIgnoreCase(qName)) {
			HashMap<String, String> itemMap = new HashMap<>();
			for(int i=0, length=attributes.getLength(); i<length; i++) {
				itemMap.put(attributes.getLocalName(i), attributes.getValue(i).trim());
			}
			itemsList.add(itemMap);
		}
	}
	
	public List<HashMap<String, String>> getItemsList() {
		return itemsList;
	}
}
