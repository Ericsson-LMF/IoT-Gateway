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

/**
 * Defines information about the CoAP message format, for example the length of
 * version bits. This implementation is based on the coap core 07 draft.
 */
public interface CoAPMessageFormat {

    /**
     * Version (Ver): 2-bit unsigned integer. Indicates the CoAP version number.
     * Implementations of this specification MUST set this field to 1. Other
     * values are reserved for future versions
     */
    int VERSION_BYTE = 0;
    int VERSION_LENGTH = 2;
    int VERSION_START = 6;

    /**
     * Type (T): 2-bit unsigned integer. Indicates if this message is of type
     * Confirmable (0), Non-Confirmable (1), Acknowledgement (2) or Reset (3).
     */
    int TYPE_BYTE = 0;
    int TYPE_LENGTH = 2;
    int TYPE_START = 4;

    /**
     * Option Count (OC): 4-bit unsigned integer. Indicates the number of
     * options after the header. If set to 0, there are no options and the
     * payload (if any) immediately follows the header. The format of options is
     * defined below.
     */
    int OPTION_BYTE = 0;
    int OPTION_COUNT_LENGTH = 4;
    int OPTION_COUNT_START = 0;

    /**
     * Code: 8-bit unsigned integer. Indicates if the message carries a request
     * (1-31) or a response (64-191), or is empty (0). (All other code values
     * are reserved.) In case of a request, the Code field indicates the Request
     * Method; in case of a response a Response Code. Possible values are
     * maintained in the CoAP Code Registry (Section 11.1). See Section 5 for
     * the semantics of requests and responses.
     */
    int CODE_BYTE = 1;
    int CODE_LENGTH = 8;
    int CODE_START = 0;

    /**
     * Option delta.
     */
    int OPTION_DELTA_LENGTH = 4;
    int OPTION_DELTA_START = 4;

    /**
     * Option length
     */
    int OPTION_LENGTH_LENGTH = 4;
    int OPTION_LENGTH_START = 0;

}
