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

import java.util.Vector;

/**
 * This class represents the enums for different CoAP option headers.
 * 
 * The options in this class are from draft-ieft-core-coap-08,
 * draft-ietf-core-observe-03 and draft-ietf-core-block-03
 * 
 */
public class CoAPOptionName {
	private final String name;
	private final int number;
	private static final Vector options = new Vector(21);

	public static final CoAPOptionName CONTENT_TYPE = new CoAPOptionName(1,
			"Content-Type");
	public static final CoAPOptionName MAX_AGE = new CoAPOptionName(2,
			"Max-Age");
	public static final CoAPOptionName PROXY_URI = new CoAPOptionName(3,
			"Proxy-Uri");
	public static final CoAPOptionName ETAG = new CoAPOptionName(4, "ETag");
	public static final CoAPOptionName URI_HOST = new CoAPOptionName(5,
			"Uri-Host");
	public static final CoAPOptionName LOCATION_PATH = new CoAPOptionName(6,
			"Location-Path");
	public static final CoAPOptionName URI_PORT = new CoAPOptionName(7,
			"Uri-Port");
	public static final CoAPOptionName LOCATION_QUERY = new CoAPOptionName(8,
			"Location-Query");
	public static final CoAPOptionName URI_PATH = new CoAPOptionName(9,
			"Uri-Path");
	public static final CoAPOptionName OBSERVE = new CoAPOptionName(10,
			"Observe");
	public static final CoAPOptionName TOKEN = new CoAPOptionName(11, "Token");
	public static final CoAPOptionName ACCEPT = new CoAPOptionName(12, "Accept");
	// The functionality by the If-Match header is not really handled by the
	// gateway
	public static final CoAPOptionName IF_MATCH = new CoAPOptionName(13,
			"If-Match");
	// New option header from draft-ietf-core-observe-03
	public static final CoAPOptionName MAX_OFE = new CoAPOptionName(14,
			"Max-OFE");
	public static final CoAPOptionName URI_QUERY = new CoAPOptionName(15,
			"Uri-Query");
	public static final CoAPOptionName BLOCK2 = new CoAPOptionName(17, "Block2");
	public static final CoAPOptionName BLOCK1 = new CoAPOptionName(19, "Block1");
	// Added from draft-ietf-core-coap-08
	public static final CoAPOptionName IF_NONE_MATCH = new CoAPOptionName(21,
			"If-None-Match");

	private CoAPOptionName(int number, String name) {
		this.number = number;
		this.name = name;
		options.add(this);
	}

	public String getName() {
		return this.name;
	}

	public int getNo() {
		return this.number;
	}

	public static CoAPOptionName getOptionName(int no) {
		for (int i = 0; i < options.size(); i++) {
			CoAPOptionName n = (CoAPOptionName) options.get(i);
			if (n.getNo() == no) {
				return n;
			}
		}

		return null;
	}

	public static CoAPOptionName getOptionNo(String name) {
		
		for (int i = 0; i < options.size(); i++) {
			CoAPOptionName n = (CoAPOptionName) options.get(i);
			if (n.getName().equals(name)) {
				return n;
			}
		}

		return null;
	}

	public String toString() {
		return number + ", " + name;
	}
}
