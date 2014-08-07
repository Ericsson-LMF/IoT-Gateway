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
 * response codes defined in core draft 11 (including obsoleted codes)
 */
public enum CoAPResponseCode implements CoAPCode {

    EMPTY(0, 0, "Empty"),
    CREATED(2, 1, "Created"),
    DELETED(2, 2, "Deleted"),
    VALID(2, 3, "Valid"),
    CHANGED(2, 4, "Changed"),
    CONTENT(2, 5, "Content"),
    BAD_REQUEST(4, 0, "Bad Request"),
    UNAUTHORIZED(4, 1, "Unauthorized"),
    BAD_OPTION(4, 2, "Bad Option"),
    FORBIDDEN(4, 3, "Forbidden"),
    NOT_FOUND(4, 4, "Not Found"),
    METHOD_NOT_ALLOWED(4, 5, "Method Not Allowed"),
    NOT_ACCEPTABLE(4, 6, "Not Acceptable"), // draft-ietf-core-coap-08
    REQUEST_ENTITY_INCOMPLETE(4, 8, "Request Entity Incomplete"), // draft-ietf-core-block-07
    PRECONDITION_FAILED(4, 12, "Precondition Failed"), // draft-ietf-core-block-11
    REQUEST_ENTITY_TOO_LARGE(4, 13, "Request Entity Too Large"),
    UNSUPPORTED_MEDIA_TYPE(4, 15, "Unsupported Media Type"),
    INTERNAL_SERVER_ERROR(5, 0, "Internal Server Error"),
    NOT_IMPLEMENTED(5, 1, "Not Implemented"),
    BAD_GATEWAY(5, 2, "Bad Gateway"),
    SERVICE_UNAVAILABLE(5, 3, "Service Unavailable"),
    GATEWAY_TIMEOUT(5, 4, "Gateway Timeout"),
    PROXYING_NOT_SUPPORTED(5, 5, "Proxying Not Supported");

    /**
     * response class is either 2 (success),4 (client error) or 5 (server
     * error). empty message will return a response class 0 (even though it's
     * not a real response)
     */
    private final int responseClass;
    private final int detail;
    private final String description;

    private CoAPResponseCode(int responseClass, int detail, String description) {
        this.responseClass = responseClass;
        this.detail = detail;
        this.description = description;
    }

    public boolean isCacheable() {
        // Valid
        if (this == VALID) {
            return true;
        }
        // Content
        if (this == CONTENT) {
            return true;
        }
        // Client Error 4.xx - Responses of this class are cacheable
        if (this.responseClass == 4) {
            return true;
        }
        // Server Error 5.xx - Responses of this class are cacheable
        return this.responseClass == 5;
    }

    /**
     * Returns the response class. 2 for success, 4 client error and 5 for
     * server error. If the response is empty, the returned response class will
     * be 0.
     *
     * @return
     */
    @Override
    public int getCodeClass() {
        return responseClass;
    }

    @Override
    public int getCodeDetail() {
        return detail;
    }

    @Override
    public String getPlainDescription() {
        return description;
    }

    private static final Map<Integer, CoAPResponseCode> noMap = new HashMap<>();

    static {
        for (CoAPResponseCode content : CoAPResponseCode.values()) {
            noMap.put(content.getNo(), content);
        }
    }

    public static CoAPResponseCode getResponseName(int no) {
        return noMap.get(no);
    }

    public static boolean isValid(int no) {
        return noMap.containsKey(no);
    }
}
