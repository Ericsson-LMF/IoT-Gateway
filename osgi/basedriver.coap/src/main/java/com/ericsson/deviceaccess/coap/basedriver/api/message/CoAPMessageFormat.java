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

import java.io.IOException;

/**
 * Defines information about the CoAP message format, for example the length of
 * version bits. This implementation is based on the coap core 07 draft.
 */
public interface CoAPMessageFormat {

    /**
     * Version (Ver): 2-bit unsigned integer. Indicates the CoAP version number.
     * Implementations of this specification MUST set this field to 1. Other
     * values are reserved for future versions. Messages with unknown version
     * numbers MUST be silently ignored.
     */
    int VERSION_LENGTH = 2;
    int VERSION_START = 6;

    /**
     * Type (T): 2-bit unsigned integer. Indicates if this message is of type
     * Confirmable (0), Non-Confirmable (1), Acknowledgement (2) or Reset (3).
     */
    int TYPE_LENGTH = 2;
    int TYPE_START = 4;

    /**
     * Token Length (TKL): 4-bit unsigned integer. Indicates the length of the
     * variable-length Token field (0-8 bytes). Lengths 9-15 are reserved, MUST
     * NOT be sent, and MUST be processed as a message format error.
     */
    int TOKEN_LENGTH_LENGTH = 4;
    int TOKEN_LENGTH_START = 0;

    /**
     * Code: 8-bit unsigned integer. It's split into a 3-bit class (most
     * significant bits) and a 5-bit detail (least significant bits), documented
     * as "c.dd" where "c" is a digit from 0 to 7 for the 3-bit subfield and
     * "dd" are two digits from 00 to 31 for the 5-bit subfield. The class can
     * indicate a request (0), a success response (2), a client error response
     * (4), or a server error response (5). (All other class values are
     * reserved.) As a special case, Code 0.00 indicates an Empty message. In
     * case of a request, the Code field indicates the Request Method; in case
     * of a response, a Response Code.
     */
    int CODE_LENGTH = 8;
    int CODE_START = 0;

    /**
     * Option delta.
     */
    int OPTION_DELTA_LENGTH = 4;
    int OPTION_DELTA_START = 4;

    int ADDITIONAL_DELTA = 13;
    int ADDITIONAL_DELTA_2 = 14;
    int PAYLOAD_MARKER = 15;

    int ADDITIONAL_DELTA_MAX = 256 + ADDITIONAL_DELTA;

    /**
     * Option length.
     */
    int OPTION_LENGTH_LENGTH = 4;
    int OPTION_LENGTH_START = 0;

    int ADDITIONAL_LENGTH = 13;
    int ADDITIONAL_LENGTH_2 = 14;
    int RESERVED = 15;

    int ADDITIONAL_LENGTH_MAX = 256 + ADDITIONAL_LENGTH;

    public class IncorrectMessageException extends IOException {

        public IncorrectMessageException(String description) {
            super(description);
        }
    }
}
