/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 * 
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,
 
 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 
 * 
 */

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
