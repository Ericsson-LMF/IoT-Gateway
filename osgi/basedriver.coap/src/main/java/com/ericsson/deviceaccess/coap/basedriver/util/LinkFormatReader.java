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

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPActivator;
import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.api.resources.CoAPResource;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * This class is responsible for parsing received link format data into CoAP
 * resources. The implementation is based on draft-ietf-core-link-format-07 and
 * RFC 5988, but is not a full implementation of these
 */
public class LinkFormatReader {

	/**
	 * Constructor
	 */
	public LinkFormatReader() {

	}

	/**
	 * This method will parse a string that is of MIME type
	 * application/link-format
	 * 
	 * @param linkFormatString
	 * @return list of CoAPResources that were included in the
	 * @throws URISyntaxException
	 * @throws CoAPException
	 */
	public List parseLinkFormatData(String linkFormatString)
			throws URISyntaxException, CoAPException {

		// multiple link descriptions are separated by commas
		// TODO fix me: if resource description contains a comma!!
		StringTokenizer links = new StringTokenizer(linkFormatString, ",");

		List lines = new LinkedList();
		int index = 0;

		while (links.hasMoreTokens()) {
			String token = links.nextToken();

			if (tokenValid(token)) {
				lines.add(token);
				index++;
			} else if (!tokenValid(token)) {
				if (index > 0) {
					String previous = (String) lines.get(index - 1);
					lines.remove(index - 1);
					token = previous + "," + token;
					lines.add(token);
				} else {
					/*
						CoAPActivator.logger.warn("Could not handle token: "
								+ token);
					*/

				}
			}
		}

		List resources = new LinkedList();

		for (int i = 0; i < lines.size(); i++) {
			CoAPResource resource = this.parseLinkFormat((String) lines.get(i));
			resources.add(resource);
		}

		return resources;
	}

	private boolean tokenValid(String token) {
		if (token.startsWith("<") && (token.indexOf(">") != -1)) {
			return true;
		}
		return false;
	}

	/**
	 * Helper method to parse each line
	 * 
	 * @param linkFormat
	 * @throws URISyntaxException
	 * @throws CoAPException
	 */
	private CoAPResource parseLinkFormat(String linkFormat)
			throws URISyntaxException, CoAPException {
		// uri is inside angle brackets "<" & ">"

		// based on discussion from
		// http://stackoverflow.com/questions/163360/regular-expresion-to-match-urls-java/163539#163539
		String uriString = "";
		if (linkFormat.endsWith(CoAPResource.DELIMITER)) {
			linkFormat = linkFormat.substring(0, linkFormat.length() - 1);
		}

		// Trim "<" and ">"

		int startIndex = 0;

		if (linkFormat.startsWith("</")) {
			uriString = linkFormat.substring(2);
			startIndex = 2;
		} else if (linkFormat.startsWith("<")) {
			uriString = linkFormat.substring(1);
			startIndex = 1;
		}

		StringTokenizer t = new StringTokenizer(uriString, ">");

		if (t.hasMoreTokens()) {
			uriString = t.nextToken();
		}

		// trim
		if (uriString.startsWith("/")) {
			uriString = uriString.substring(1);
		}

		// First token is the URI, trim it
		URI uri = new URI(uriString);

		CoAPResource resource = new CoAPResource(uri);

		String attributes = linkFormat.substring(uriString.length()
				+ startIndex + 1);

		t = new StringTokenizer(attributes, ";");

		while (t.hasMoreTokens()) {
			String attribute = t.nextToken();

			StringTokenizer st = new StringTokenizer(attribute, "=");
			String attributeName = "";
			String attributeValue = "";
			if (st.countTokens() == 2) {
				int i = 0;
				while (st.hasMoreTokens()) {
					if (i == 0) {
						attributeName = st.nextToken();
					} else {
						attributeValue = st.nextToken();
					}
					i++;
				}
			}

			// populate attributes // These are the extensions defined in the
			// CoRE link format 07
			if (attributeName.equals(CoAPResource.RESOURCE_TYPE)) {
				if (attributeValue.startsWith("\"")
						&& attributeValue.endsWith("\"")) {
					resource.setResourceType(attributeValue);
				}
			} else if (attributeName.equals(CoAPResource.INTERFACE_DESCRIPTION)) {
				if (attributeValue.startsWith("\"")
						&& attributeValue.endsWith("\"")) {
					resource.setInterfaceDescription(attributeValue);
				}
			} else if (attributeName.equals(CoAPResource.MAXIMUM_SIZE)) { // cast
				if (attributeValue.startsWith("\"")) {
					attributeValue = attributeValue.substring(1);
				}
				if (attributeValue.endsWith("\"")) {
					attributeValue = attributeValue.substring(0,
							attributeValue.length() - 1);
				}
				// int
				int maximumSize = Integer.parseInt(attributeValue);
				resource.setMaximumSize(maximumSize);
			} else if (attributeName.equals(CoAPResource.TITLE)) {
				if (resource.getTitle().equals("")) {
					resource.setTitle(attributeValue);
				} // If title is present, it should be used instead of title
			} else if (attributeName.equals(CoAPResource.TITLE_ASTERISK)) {
				if (resource.getTitleAsterisk().equals("")) {
					resource.setTitleAsterisk(attributeValue);
				}
			} else if (attributeName.equals(CoAPResource.RELATION_TYPE)) {

				// relation type is a low case string or a URI
				if (resource.getRelationType().equals("")) {
					resource.setRelationType(attributeValue);
				}
			} else if (attributeName.equals(CoAPResource.ANCHOR)) {
				resource.setAnchor(attributeValue);
			} else if (attributeName.equals(CoAPResource.CONTENT_TYPE)) {
				int contentTypeInt = Integer.parseInt(attributeValue);
				resource.setContentType(contentTypeInt);
			} else if (attributeName.equals(CoAPResource.INSTANCE)) {
				resource.setInstance(attributeValue);
			} else if (attributeName.equals(CoAPResource.KEY)) {
				resource.setKey(attributeValue);
			}
		}
		return resource;
	}
}
