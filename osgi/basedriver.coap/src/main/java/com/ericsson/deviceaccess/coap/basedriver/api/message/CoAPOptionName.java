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
 * This class represents the enums for different CoAP option headers.
 *
 * The options in this class are from draft-ieft-core-coap-08,
 * draft-ietf-core-observe-03 and draft-ietf-core-block-03
 *
 */
public enum CoAPOptionName {

    UNKNOWN(0, "Unknown"),
    // The functionality by the If-Match header is not really handled by the gateway
    IF_MATCH(1, "If-Match", 0, 8, true),
    URI_HOST(3, "Uri-Host", 1, 255),
    ETAG(4, "ETag", 1, 8, true),
    // Added from draft-ietf-core-coap-08
    IF_NONE_MATCH(5, "If-None-Match"),
    OBSERVE(6, "Observe", 0, 3),
    URI_PORT(7, "Uri-Port", 0, 2),
    LOCATION_PATH(8, "Location-Path", 0, 255, true),
    URI_PATH(11, "Uri-Path", 0, 255, true),
    CONTENT_FORMAT(12, "Content-Format", 0, 2),
    MAX_AGE(14, "Max-Age", 0, 4),
    URI_QUERY(15, "Uri-Query", 0, 255, true),
    ACCEPT(17, "Accept", 0, 2),
    LOCATION_QUERY(20, "Location-Query", 0, 255, true),
    BLOCK2(23, "Block2", 0, 3),
    BLOCK1(27, "Block1", 0, 3),
    PROXY_URI(35, "Proxy-Uri", 1, 1034),
    PROXY_SCHEME(39, "Proxy-Scheme", 1, 255),
    SIZE1(60, "Size1", 0, 4);

    private final String name;
    private final int number;
    private final int min;
    private final int max;
    private boolean repeatable;

    private CoAPOptionName(int number, String name, int min, int max) {
        this(number, name, min, max, false);
    }

    private CoAPOptionName(int number, String name, int min, int max, boolean repeatable) {
        this.repeatable = repeatable;
        this.min = min;
        this.max = max;
        this.number = number;
        this.name = name;
    }

    private CoAPOptionName(int number, String name) {
        min = 0;
        max = 0;
        repeatable = false;
        this.number = number;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getNo() {
        return number;
    }

    /**
     * This method can be used to check if the option is critical. Odd number
     * means a critical option.
     *
     * @return true if this option is critical (odd), false otherwise
     */
    public boolean isCritical() {
        return number % 2 == 1;
    }

    public boolean isLegalSize(int size) {
        return min <= size && size <= max;
    }

    public boolean isRepeatable() {
        return repeatable;
    }

    private static final Map<String, CoAPOptionName> nameMap = new HashMap<>();
    private static final Map<Integer, CoAPOptionName> noMap = new HashMap<>();

    static {
        for (CoAPOptionName option : CoAPOptionName.values()) {
            nameMap.put(option.getName(), option);
            noMap.put(option.getNo(), option);
        }
    }

    public static CoAPOptionName getFromNo(int no) {
        return noMap.getOrDefault(no, UNKNOWN);
    }

    public static CoAPOptionName getFromName(String name) {
        return nameMap.getOrDefault(name, UNKNOWN);
    }
}
