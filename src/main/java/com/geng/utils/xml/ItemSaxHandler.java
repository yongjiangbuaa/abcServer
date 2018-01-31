/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年11月12日 下午2:55:33
 */
package com.geng.utils.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Map;

/**
 * 读取单条XML
 */
public class ItemSaxHandler extends DefaultHandler {
	private ConfigMap itemConfigMap;
	private String itemId;
	
	public ItemSaxHandler(String itemId) {
		itemConfigMap = new ConfigMap();
		this.itemId = itemId;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		if(Constants.XML_ELEMENT_NAME.equalsIgnoreCase(qName) && attributes.getValue("id").equalsIgnoreCase(itemId)) {
			for(int i=0, length=attributes.getLength();i<length; i++) {
				itemConfigMap.put(attributes.getLocalName(i), attributes.getValue(i).trim());
			}
			super.endDocument();
			throw new SAXException("find");
		}
	}

	public Map<String, String> getItem() {
		return itemConfigMap;
	}
}
