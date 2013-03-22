package com.ericsson.deviceaccess.upnp;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Stack;
import java.util.Vector;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import com.ericsson.deviceaccess.upnp.media.MediaContainer;
import com.ericsson.deviceaccess.upnp.media.MediaItem;

public class DidlXmlPullParser {
	private static final String NS_URI_DC = "http://purl.org/dc/elements/1.1/";
	private static final String NS_URI_UPNP = "urn:schemas-upnp-org:metadata-1-0/upnp/";
	private static final String NS_URI_DIDL = "urn:schemas-upnp-org:metadata-1-0/DIDL-Lite/";

	private static final String ID = "id";
	private static final String TITLE = "title";
	private static final String URL = "url";
	private static final String ARTIST = "artist";
	private static final String ALBUMART_URI = "albumart";
	private static final String RESOLUTION = "resolution";
	private static final String DURATION = "duration";
	private static final String PROTOCOL_INFO = "protocolInfo";
	private static final String MIME_TYPE = "mimeType";
	private static final String UPNP_CLASS = "class";
	private static final String SIZE = "size";
	private static final String TAG_ATTRIBUTES = "tagAttributes";


    public static Vector parseDidl(String didl) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(didl));
        int eventType = xpp.getEventType();

        Vector results = new Vector();
        Properties properties = new Properties();
        Stack tagStack = new Stack();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
                case XmlPullParser.START_TAG: {
                    final String tag = xpp.getName();
                    tagStack.push(tag);
                    if (tag.equals("container") || tag.equals("item")) {
                        properties.setProperty(ID, xpp.getAttributeValue(null, ID));
                    } else if (tag.equals("res")) {
                        properties.setProperty("protocolInfo", xpp.getAttributeValue(null, "protocolInfo"));
                        int nAttr = xpp.getAttributeCount();
                        Map attributes = new HashMap();
                        for (int i = 0; i < nAttr; i++) {
                            attributes.put(xpp.getAttributeName(i), xpp.getAttributeValue(i));
                        }
                    }


                    break;
                }
                case XmlPullParser.END_TAG: {
                    String tag = (String) tagStack.pop();
                    if (tag.equals("container")) {
                    	MediaContainer container = new MediaContainer();
                    	container.setId(properties.getProperty(ID));
                    	container.setName(properties.getProperty(TITLE));
                        properties.clear();
                        results.add(container);
                    } else if (tag.equals("item")) {
                    	MediaItem item = new MediaItem();
                    	item.setId(properties.getProperty(ID));
                    	item.setName(properties.getProperty(TITLE));
                    	item.setUrl(properties.getProperty(URL));
                    	item.setType(getTypeFromUpnpClass(properties.getProperty(UPNP_CLASS)));
                        properties.clear();
                        results.add(item);
                    }

                    break;
                }
                case XmlPullParser.TEXT: {
                    String tag = (String) tagStack.peek();
                    if (tag.equals("title")) {
                        properties.setProperty(TITLE, xpp.getText());
                    } else if (tag.equals("res")) {
                        properties.setProperty(URL, xpp.getText());
                    } else if (tag.indexOf("artist") >= 0) {
                    	properties.setProperty(ARTIST, xpp.getText());
                    } else if (tag.indexOf("albumArtURI") >= 0) {
                        properties.setProperty(ALBUMART_URI, xpp.getText());
                    } else if (tag.indexOf(UPNP_CLASS) >= 0) {
                        properties.setProperty(UPNP_CLASS, xpp.getText());
                    }
                    break;
                }
                default:
                    break;

            }

            eventType = xpp.next();
        }
        return results;
    }


	private static String getTypeFromUpnpClass(String className) {
		if(className == null) return MediaItem.TYPE_UNKNOWN;
				
		if(className.indexOf("video") >= 0){
			return MediaItem.TYPE_VIDEO;
		} else if(className.indexOf("audio") >= 0){
			return MediaItem.TYPE_AUDIO;
		} else if(className.indexOf("image") >= 0){
			return MediaItem.TYPE_IMAGE;
		}
		return MediaItem.TYPE_UNKNOWN;
	}
}
