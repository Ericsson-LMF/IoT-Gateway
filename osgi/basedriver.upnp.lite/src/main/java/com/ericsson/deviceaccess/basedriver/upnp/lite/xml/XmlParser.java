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
            if (parser.getEventType() != XmlPullParser.END_DOCUMENT) {
                return node;
            }
            throw new Exception("Illegal XML state: " + parser.getName() + ", " + parser.getEventType());
        } else {
            node.name = parser.getName();

            for (int i = 0; i < parser.getAttributeCount(); i++) {
                node.attributes.put(parser.getAttributeName(i), parser.getAttributeValue(i));
            }

            parser.next();

            while (parser.getEventType() != XmlPullParser.END_TAG) {
                if (parser.getEventType() == XmlPullParser.START_TAG) {
                    node.children.add(_parse(parser, ignoreWhitespaces));
                } else if (parser.getEventType() == XmlPullParser.TEXT) {
                    if (!ignoreWhitespaces || !parser.isWhitespace()) {
                        node.text += parser.getText();
                    }
                }
                parser.next();
            }
        }
        return node;
    }

    private XmlParser() {
    }
}
