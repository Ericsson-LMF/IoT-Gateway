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
package com.ericsson.deviceaccess.coap.basedriver.osgi;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;

import com.ericsson.deviceaccess.coap.basedriver.api.CoAPException;
import com.ericsson.deviceaccess.coap.basedriver.util.NetUtil;

/**
 * This class is used for creating a singleton instance of the
 * LocalCoAPEndpoint.
 */
public class CoAPEndpointFactory {

	private static CoAPEndpointFactory coapEndpointFactory;
	private LocalCoAPEndpoint localEndpoint;

	/**
	 * Constructor is private, use getInstance method to get an instance of the
	 * factory.
	 */
	private CoAPEndpointFactory() {
		localEndpoint = null;
	}

	/**
	 * Returns a singleton instance of this factory. Creates a new one if one
	 * doesn't exist, or if one already exists, returns that one.
	 * 
	 * @return a singleton instance of the CoAPEndpointFactory
	 */
	public static synchronized CoAPEndpointFactory getInstance() {
		if (coapEndpointFactory == null) {
			coapEndpointFactory = new CoAPEndpointFactory();
		}
		return coapEndpointFactory;
	}

	/**
	 * Returns a singleton instance of the LocalCoAPEndpoint
	 * 
	 * @return singleton instance of the LocalCoAPEndpoint
	 */
	public LocalCoAPEndpoint createLocalCoAPEndpoint(
			OutgoingMessageHandler outgoingHandler,
			IncomingMessageHandler incomingHandler, InetAddress address,
			int port) throws CoAPException {
		if (address == null) {
			address = NetUtil.getMyInetAddress(NetUtil.ADDR_SCOPE_PRIORITISED_IPV6);
		}
		if (localEndpoint == null) {
			synchronized (this) {
				if (this.localEndpoint == null) {
					String hostAddress = address.getHostAddress();
					try {
						URI uri = new URI(null, null, hostAddress, port, null,
								null, null);
						localEndpoint = new LocalCoAPEndpoint(outgoingHandler,
								incomingHandler, uri);
					} catch (URISyntaxException e) {
						e.printStackTrace();
						throw new CoAPException(e);
					}
				}
			}
		}
		return localEndpoint;
	}

	/**
	 * Get the local endpoint. Returns null, if no endpoint has been created
	 * yet.
	 * 
	 * @return local endpoint or null if no endpoint has been created
	 */
	public LocalCoAPEndpoint getLocalCoAPEndpoint() {
		return localEndpoint;
	}

	/**
	 * This method is called when the bundle is stopped. The local endpoint will
	 * be set to null.
	 */
	public static synchronized void stopService() {
		coapEndpointFactory.localEndpoint = null;
	}
}
