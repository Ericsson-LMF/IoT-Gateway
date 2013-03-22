package com.ericsson.deviceaccess.basedriver.upnp.lite.xml;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import org.osgi.service.upnp.UPnPAction;

public class XmlNode {
	protected String name = null;
	protected String text = "";
	protected Vector children = null;
	public Hashtable attributes = null;

	public XmlNode() {
		this.children = new Vector();
		this.attributes = new Hashtable();
	}

	public String getText() {
		return text;
	}

	public String getName() {
		return name;
	}

	public Hashtable getAttributes() {
		return attributes;
	}

	public String[] getAttributeNames() {
		String[] names = new String[attributes.size()];

		Enumeration e = attributes.keys();

		int i = 0;

		while (e.hasMoreElements()) {
			names[i] = (String) e.nextElement();

			i++;
		}
		return names;
	}

	public String getAttribute(String key) {
		return (String) attributes.get(key);
	}

	public XmlNode[] getChildren() {
		return (XmlNode[]) children.toArray(new XmlNode[0]);
	}
	
	public XmlNode[] getChildren(String name) {
		Vector matches = new Vector();
		for(int i = 0; i < children.size(); i++) {
			XmlNode node = (XmlNode) children.elementAt(i);
			if (name.equals(node.name))
				matches.add(node);
		}
		return (XmlNode[]) matches.toArray(new XmlNode[0]);
	}
	
	public XmlNode getChild(String name) {
		for(int i = 0; i < children.size(); i++) {
			XmlNode node = (XmlNode) children.elementAt(i);
			if (name.equals(node.name))
				return node;
		}
		return null;
	}
}