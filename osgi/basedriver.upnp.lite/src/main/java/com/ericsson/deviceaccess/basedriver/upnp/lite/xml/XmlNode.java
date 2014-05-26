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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


public class XmlNode {

    protected String name = null;
    protected String text = "";
    protected List<XmlNode> children = null;
    public ConcurrentHashMap attributes = null;

    public XmlNode() {
        this.children = Collections.synchronizedList(new ArrayList<XmlNode>());
        this.attributes = new ConcurrentHashMap();
    }

    public String getText() {
        return text;
    }

    public String getName() {
        return name;
    }

    public ConcurrentHashMap getAttributes() {
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
        List<XmlNode> matches = Collections.synchronizedList(new ArrayList<XmlNode>());
        //TODO synchronization if needed.
        for (XmlNode node : children) {
            if (name.equals(node.name)) {
                matches.add(node);
            }
        }
        return (XmlNode[]) matches.toArray(new XmlNode[0]);
    }

    public XmlNode getChild(String name) {
        for (XmlNode node : children) {
            if (name.equals(node.name)) {
                return node;
            }
        }
        return null;
    }
}
