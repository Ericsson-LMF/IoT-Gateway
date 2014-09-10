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
package com.ericsson.deviceaccess.coap.basedriver.util;

import com.ericsson.common.util.BitUtil;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPContentType;
import com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionHeader;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.ACCEPT;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.BLOCK1;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.BLOCK2;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.CONTENT_FORMAT;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.ETAG;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.IF_MATCH;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.IF_NONE_MATCH;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.LOCATION_PATH;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.LOCATION_QUERY;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.MAX_AGE;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.OBSERVE;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.PROXY_URI;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_HOST;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_PATH;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_PORT;
import static com.ericsson.deviceaccess.coap.basedriver.api.message.CoAPOptionName.URI_QUERY;
import java.nio.charset.StandardCharsets;

/**
 * This is a helper class to convert option header to string format from byte
 * arrays
 *
 */
public class CoAPOptionHeaderConverter {


    private static final char[] HEX_CHARS = new char[]{
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    /**
     * Constructor. Creates an instance of the converter.
     */
    public CoAPOptionHeaderConverter() {

    }

    /**
     * Convert the value of the option header to string. There are different
     * formats for option headers, so different headers are handled differently
     * based on the format. For now, Etag, Token and If-Match are converted to
     * hex strings.
     *
     * @param header header whose value is to be converted
     * @return string representation of the header value
     */
    public String convertOptionHeaderToString(CoAPOptionHeader header) {
        byte[] bytes = header.getValue();
        switch (header.getOptionName()) {
            case CONTENT_FORMAT:
                return CoAPContentType.get(BitUtil.shortToUnsignedInt(bytes)).getContentType();
            case URI_PORT:
            case ACCEPT:
            case OBSERVE:
                return Integer.toString(BitUtil.shortToUnsignedInt(bytes));
            case MAX_AGE:
                return Long.toString(BitUtil.convertIntToUnsignedLong(bytes));
            case PROXY_URI:
            case URI_HOST:
            case URI_QUERY:
            case URI_PATH:
            case LOCATION_QUERY:
            case LOCATION_PATH:
                return new String(bytes, StandardCharsets.UTF_8);
            case ETAG:
            case IF_MATCH: // TODO etag and if-match
                // TODO check if the byte array is hex or string??
                return convertHexToString(bytes);
            case BLOCK1:
            case BLOCK2:
                // show the value as unsigned int
                long longValue = BitUtil.convertIntToUnsignedLong(bytes);
                // More details for debug (Ryoji)
                //value = Long.toString(longValue);
                long num = longValue >> 4;
                long m = (longValue >> 3) & 0x1L;
                long szx = longValue & 7L;
                return longValue + " (Num=" + num + "/M=" + m + "/Sz=" + CoAPUtil.getBlockSize(szx) + ")";
            case IF_NONE_MATCH:
            default:
                return "";
        }
    }

    public boolean isHex(String in) {
        boolean isHex = false;
        try {
            // try to parse the string to an integer, using 16 as radix
            Integer.parseInt(in, 16);
            isHex = true;
        } catch (NumberFormatException e) {
        }
        return isHex;
    }

    /**
     * Convert value of the option header byte array to a hex string
     *
     * @param header header to be converted
     * @return hex string representation of a byte array
     */
    private String convertHexToString(byte[] bytes) {
        StringBuilder buff = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            buff.insert(i * 2, HEX_CHARS[(bytes[i] >> 4) & 0xf]);
            buff.insert(i * 2 + 1, HEX_CHARS[bytes[i] & 0xf]);
        }
        return buff.toString();
    }

}
