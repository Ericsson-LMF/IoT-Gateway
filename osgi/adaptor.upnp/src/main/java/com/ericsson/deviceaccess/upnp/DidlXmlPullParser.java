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

import com.ericsson.commonutil.function.QuadConsumer;
import com.ericsson.deviceaccess.upnp.media.MediaContainer;
import com.ericsson.deviceaccess.upnp.media.MediaItem;
import com.ericsson.deviceaccess.upnp.media.MediaObject;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

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

    private static final Map<Integer, QuadConsumer<XmlPullParser, Stack<String>, Map<String, String>, List<MediaObject>>> eventTypes = new HashMap<>();
    private static QuadConsumer<XmlPullParser, Stack<String>, Map<String, String>, List<MediaObject>> EMPTY = (a, b, c, d) -> {
    };

    static {
        eventTypes.put(XmlPullParser.START_TAG, (xpp, tagStack, properties, results) -> {
            String tag = xpp.getName();
            tagStack.push(tag);
            switch (tag) {
                case "container":
                case "item":
                    properties.put(ID, xpp.getAttributeValue(null, ID));
                    break;
                case "res":
                    properties.put("protocolInfo", xpp.getAttributeValue(null, "protocolInfo"));
                    int nAttr = xpp.getAttributeCount();
                    Map<String, String> attributes = new HashMap<>();
                    for (int i = 0; i < nAttr; i++) {
                        attributes.put(xpp.getAttributeName(i), xpp.getAttributeValue(i));
                    }
                    break;
            }
        });
        eventTypes.put(XmlPullParser.END_TAG, (xpp, tagStack, properties, results) -> {
            switch (tagStack.pop()) {
                case "container":
                    MediaContainer container = new MediaContainer();
                    container.setId(properties.get(ID));
                    container.setName(properties.get(TITLE));
                    properties.clear();
                    results.add(container);
                    break;
                case "item":
                    MediaItem item = new MediaItem();
                    item.setId(properties.get(ID));
                    item.setName(properties.get(TITLE));
                    item.setUrl(properties.get(URL));
                    item.setType(getTypeFromUpnpClass(properties.get(UPNP_CLASS)));
                    properties.clear();
                    results.add(item);
                    break;
            }
        });
        eventTypes.put(XmlPullParser.TEXT, (xpp, tagStack, properties, results) -> {
            String tag = tagStack.peek();
            if (tag.equals("title")) {
                properties.put(TITLE, xpp.getText());
            } else if (tag.equals("res")) {
                properties.put(URL, xpp.getText());
            } else if (tag.contains("artist")) {
                properties.put(ARTIST, xpp.getText());
            } else if (tag.contains("albumArtURI")) {
                properties.put(ALBUMART_URI, xpp.getText());
            } else if (tag.contains(UPNP_CLASS)) {
                properties.put(UPNP_CLASS, xpp.getText());
            }
        });
    }

    public static List<MediaObject> parseDidl(String didl) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();

        xpp.setInput(new StringReader(didl));
        int eventType = xpp.getEventType();

        List<MediaObject> results = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();
        Stack<String> tagStack = new Stack<>();

        while (eventType != XmlPullParser.END_DOCUMENT) {
            eventTypes.getOrDefault(eventType, EMPTY).consume(xpp, tagStack, properties, results);
            eventType = xpp.next();
        }
        return results;
    }

    private static String getTypeFromUpnpClass(String className) {
        if (className == null) {
            return MediaItem.TYPE_UNKNOWN;
        }

        if (className.contains("video")) {
            return MediaItem.TYPE_VIDEO;
        } else if (className.contains("audio")) {
            return MediaItem.TYPE_AUDIO;
        } else if (className.contains("image")) {
            return MediaItem.TYPE_IMAGE;
        }
        return MediaItem.TYPE_UNKNOWN;
    }

    private DidlXmlPullParser() {
    }
}
