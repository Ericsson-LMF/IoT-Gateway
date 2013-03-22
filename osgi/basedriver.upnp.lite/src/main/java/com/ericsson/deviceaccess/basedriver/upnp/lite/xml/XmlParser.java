package com.ericsson.deviceaccess.basedriver.upnp.lite.xml;

import java.io.Reader;

import org.kxml2.io.KXmlParser;
import org.xmlpull.v1.XmlPullParser;

public class XmlParser {
	public static XmlNode parse(Reader reader, boolean ignoreWhitespaces) throws Exception {
		KXmlParser parser = new KXmlParser();
		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		parser.setInput(reader);
		parser.next();
		XmlNode node = _parse(parser, ignoreWhitespaces);
		
		return node;
	}

	static XmlNode _parse(KXmlParser parser, boolean ignoreWhitespaces) throws Exception {
		XmlNode node = new XmlNode();

		if (parser.getEventType() != XmlPullParser.START_TAG) {
			if (parser.getEventType() != XmlPullParser.END_DOCUMENT)
				return node;
			
			throw new Exception("Illegal XML state: " + parser.getName() + ", " + parser.getEventType());
		} else {
			node.name = parser.getName();

			for (int i = 0; i < parser.getAttributeCount(); i++) {
				node.attributes.put(parser.getAttributeName(i), parser.getAttributeValue(i));
			}

			parser.next();

			while (parser.getEventType() != XmlPullParser.END_TAG) {
				if (parser.getEventType() == XmlPullParser.START_TAG) {
					node.children.addElement(_parse(parser, ignoreWhitespaces));
				} else if (parser.getEventType() == XmlPullParser.TEXT) {
					if (!ignoreWhitespaces || !parser.isWhitespace())
						node.text += parser.getText();
				}
				parser.next();
			}
		}
		return node;
	}
}