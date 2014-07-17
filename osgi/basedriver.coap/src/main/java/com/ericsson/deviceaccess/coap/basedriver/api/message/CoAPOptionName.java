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

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the enums for different CoAP option headers.
 *
 * The options in this class are from draft-ieft-core-coap-08,
 * draft-ietf-core-observe-03 and draft-ietf-core-block-03
 *
 */
public enum CoAPOptionName {

    UNKNOWN(0, "unknown"),
    CONTENT_TYPE(1, "Content-Type"),
    MAX_AGE(2, "Max-Age"),
    PROXY_URI(3, "Proxy-Uri"),
    ETAG(4, "ETag"),
    URI_HOST(5, "Uri-Host"),
    LOCATION_PATH(6, "Location-Path"),
    URI_PORT(7, "Uri-Port"),
    LOCATION_QUERY(8, "Location-Query"),
    URI_PATH(9, "Uri-Path"),
    OBSERVE(10, "Observe"),
    TOKEN(11, "Token"),
    ACCEPT(12, "Accept"),
    // The functionality by the If-Match header is not really handled by the gateway
    IF_MATCH(13, "If-Match"),
    // New option header from draft-ietf-core-observe-03
    MAX_OFE(14, "Max-OFE"),
    URI_QUERY(15, "Uri-Query"),
    BLOCK2(17, "Block2"),
    BLOCK1(19, "Block1"),
    // Added from draft-ietf-core-coap-08
    IF_NONE_MATCH(21, "If-None-Match");

    private final String name;
    private final int number;

    private CoAPOptionName(int number, String name) {
        this.number = number;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public int getNo() {
        return this.number;
    }

    public static CoAPOptionName getOptionName(int no) {
        for (CoAPOptionName option : CoAPOptionName.values()) {
            if (option.getNo() == no) {
                return option;
            }
        }
        return null;
    }

    public static CoAPOptionName getOptionNo(String name) {
        for (CoAPOptionName option : CoAPOptionName.values()) {
            if (option.getName().equals(name)) {
                return option;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return number + ", " + name;
    }
}
