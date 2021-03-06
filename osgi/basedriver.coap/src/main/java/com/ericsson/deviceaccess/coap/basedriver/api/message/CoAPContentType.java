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
package com.ericsson.deviceaccess.coap.basedriver.api.message;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is to represent the CoAP content types as defined in the coap core
 * draft media type registry
 * (http://tools.ietf.org/html/draft-ietf-core-coap-08#page-65).
 *
 */
public enum CoAPContentType {

    UNKNOWN(-1, "unkown"),
    TEXT_PLAIN(0, "text/plain; charset=utf-8"),
    LINK_FORMAT(40, "application/link-format"),
    APPLICATION_XML(41, "application/xml"),
    OCTET_STREAM(42, "application/octet-stream"),
    EXI(47, "application/exi"),
    JSON(50, "application/json");

    private final int no;
    private final String contentType;

    /**
     * Constructor
     *
     * @param no number of the content type
     * @param contentType string representing the media type
     */
    private CoAPContentType(int no, String contentType) {
        this.no = no;
        this.contentType = contentType;
    }

    public int getNo() {
        return no;
    }

    public String getContentType() {
        return contentType;
    }

    private static final Map<String, CoAPContentType> typeMap = new HashMap<>();
    private static final Map<Integer, CoAPContentType> noMap = new HashMap<>();

    static {
        for (CoAPContentType content : CoAPContentType.values()) {
            typeMap.put(content.getContentType(), content);
            noMap.put(content.getNo(), content);
        }
    }

    public static CoAPContentType get(String name) {
        return typeMap.getOrDefault(name, UNKNOWN);
    }

    public static CoAPContentType get(int no) {
        return noMap.getOrDefault(no, UNKNOWN);
    }

}
