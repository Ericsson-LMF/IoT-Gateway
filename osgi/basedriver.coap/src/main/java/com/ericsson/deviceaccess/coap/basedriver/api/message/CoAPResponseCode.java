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

import com.ericsson.deviceaccess.coap.basedriver.util.BitOperations;
import java.util.ArrayList;
import java.util.List;

/**
 * response codes defined in core draft 11 (including obsoleted codes)
 */
public class CoAPResponseCode {

	/**
	 * response class is either 2 (success),4 (client error) or 5 (server
	 * error). empty message will return a response class 0 (even though it's
	 * not a real response)
	 */
	private int responseClass;
	private final String description;
	private final int no;
	private static final List values = new ArrayList<>(20);

	public static final CoAPResponseCode CREATED = new CoAPResponseCode(65,
			"2.01 Created");
	public static final CoAPResponseCode DELETED = new CoAPResponseCode(66,
			"2.02 Deleted");
	public static final CoAPResponseCode VALID = new CoAPResponseCode(67,
			"2.03 Valid");
	public static final CoAPResponseCode CHANGED = new CoAPResponseCode(68,
			"2.04 Changed");
	public static final CoAPResponseCode CONTENT = new CoAPResponseCode(69,
			"2.05 Content");
	public static final CoAPResponseCode BAD_REQUEST = new CoAPResponseCode(
			128, "4.00 Bad Request");
	public static final CoAPResponseCode UNAUTHORIZED = new CoAPResponseCode(
			129, "4.01 Unauthorized");
	public static final CoAPResponseCode BAD_OPTION = new CoAPResponseCode(130,
			"4.02 Bad Option");
	public static final CoAPResponseCode FORBIDDEN = new CoAPResponseCode(131,
			"4.03 Forbidden");
	public static final CoAPResponseCode NOT_FOUND = new CoAPResponseCode(132,
			"4.04 Not Found");
	public static final CoAPResponseCode METHOD_NOT_ALLOWED = new CoAPResponseCode(
			133, "4.05 Method Not Allowed");
	public static final CoAPResponseCode NOT_ACCEPTABLE = new CoAPResponseCode(
			134, "4.06 Not Acceptable"); // draft-ietf-core-coap-08
	public static final CoAPResponseCode REQUEST_ENTITY_INCOMPLETE = new CoAPResponseCode(
			136, "4.08 Request Entity Incomplete"); // draft-ietf-core-block-07
	public static final CoAPResponseCode PRECONDITION_FAILED = new CoAPResponseCode(
			140, "4.12 Precondition Failed"); // draft-ietf-core-block-11
	public static final CoAPResponseCode REQUEST_ENTITY_TOO_LARGE = new CoAPResponseCode(
			141, "4.13 Request Entity Too Large");
	public static final CoAPResponseCode UNSUPPORTED_MEDIA_TYPE = new CoAPResponseCode(
			143, "4.15 Unsupported Media Type");
	public static final CoAPResponseCode INTERNAL_SERVER_ERROR = new CoAPResponseCode(
			160, "5.00 Internal Server Error");
	public static final CoAPResponseCode NOT_IMPLEMENTED = new CoAPResponseCode(
			161, "5.01 Not Implemented");
	public static final CoAPResponseCode BAD_GATEWAY = new CoAPResponseCode(
			162, "5.02 Bad Gateway");
	public static final CoAPResponseCode SERVICE_UNAVAILABLE = new CoAPResponseCode(
			163, "5.03 Service Unavailable");
	public static final CoAPResponseCode GATEWAY_TIMEOUT = new CoAPResponseCode(
			164, "5.04 Gateway Timeout");
	public static final CoAPResponseCode PROXYING_NOT_SUPPORTED = new CoAPResponseCode(
			165, "5.05 Proxying Not Supported");

	private CoAPResponseCode(int no, String description) {
		this.description = description;
		this.no = no;

		responseClass = BitOperations.getBitsInIntAsInt(this.no, 5, 3);
		values.add(this);
	}

	/**
	 * Textual description of the code as defined in draft-ietf-core-coap-08
	 * 
	 * @return Textual description of the code
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Return the number of the response
	 * 
	 * @return integer representing the code of the response
	 */
	public int getNo() {
		return this.no;
	}

	/**
	 * Returns the response class. 2 for success, 4 client error and 5 for
	 * server error. If the response is empty, the returned response class will
	 * be 0.
	 * 
	 * @return
	 */
	public int getResponseClass() {
		return this.responseClass;
	}

	public static CoAPResponseCode getResponseName(int no) {

            for (Object value : values) {
                CoAPResponseCode c = (CoAPResponseCode) value;
                if (c.getNo() == no) {
                    return c;
                }
            }

		return null;
	}

        @Override
	public String toString() {
		return no + ", " + description;
	}
}