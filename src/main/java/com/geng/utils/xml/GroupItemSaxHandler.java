/**
 * @author: lifangkai@elex-tech.com
 * @date: 2013年11月12日 下午2:55:33
 */
package com.geng.utils.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * 读取group
 */
public class GroupItemSaxHandler extends DefaultHandler {
	private List<HashMap<String, String>> groupMap;
	private String groupName;
	private boolean isFindGroup;
	
	public GroupItemSaxHandler(String groupName) {
		groupMap = new ArrayList<>();
		this.groupName = groupName;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		String itemId = attributes.getValue("id");
		if(Constants.XML_GROUP_NAME.equalsIgnoreCase(qName) && itemId.equalsIgnoreCase(groupName)) {
			isFindGroup = true;
		} else if(isFindGroup) {
			HashMap<String, String> itemMap = new HashMap<String, String>();
			for(int i=0, length=attributes.getLength();i<length; i++) {
				itemMap.put(attributes.getLocalName(i), attributes.getValue(i).trim());
			}
			groupMap.add(itemMap);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(Constants.XML_GROUP_NAME.equals(qName) && isFindGroup) {
			isFindGroup = false;
			super.endDocument();
			throw new SAXException("find");
		}
	}

	public List<HashMap<String, String>> getGroup() {
		return groupMap;
	}
}
