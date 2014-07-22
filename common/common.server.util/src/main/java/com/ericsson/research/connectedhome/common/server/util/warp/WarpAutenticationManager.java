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
package com.ericsson.research.connectedhome.common.server.util.warp;

import com.ericsson.research.connectedhome.common.BasicAuthFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;
import javax.ws.rs.core.MediaType;

/**
 * Interface towards the warp authentication manager.
 *
 * Uses the web API:
 * <ul>
 * <li>GET /AuthenticationProviderView/api/users
 * <li>GET /AuthenticationProviderView/api/origin/{origin}
 * <li>GET /AuthenticationProviderView/api/users/{user}
 * <li>PUT /AuthenticationProviderView/api/users/{user}
 * <li>DELETE /AuthenticationProviderView/api/users/{user}
 * </ul>
 * Auth = Basic<br>
 * User = admin<br>
 * PW (default) = lamepass<br>
 *
 * When doing put, Content Type must be "application/json" or "application/xml".
 * For all methods except delete, the Accept header shall be set to
 * "application/json" or "application/xml".
 */
public class WarpAutenticationManager {

    private static final String authRequestFormat = "http://{0}/Core-Authentication-View/api/{1}";

    final private Client jerseyClient;
    final private String warpServerAddr;
    final private String warpAdminUser;
    final private String warpAdminPassword;

    /**
     * Create a manager.
     *
     * @param jerseyClient Jersey client to be used to access the
     * AuthenticationProviderView
     * @param warpServerAddr the host name/IP for the warp server
     * @param warpAdminUser the admin user for AuthenticationProviderView
     * @param warpAdminPassword the admin password for
     * AuthenticationProviderView
     */
    public WarpAutenticationManager(
            Client jerseyClient, String warpServerAddr, String warpAdminUser,
            String warpAdminPassword) {
        this.jerseyClient = jerseyClient;
        this.warpServerAddr = warpServerAddr;
        this.warpAdminUser = warpAdminUser;
        this.warpAdminPassword = warpAdminPassword;
    }

    /**
     * Gets the user credentials for the specified user.
     *
     * @param userName the warp user name
     *
     * @return the user credentials
     * @throws
     * com.ericsson.research.connectedhome.common.server.util.warp.WarpAutenticationManager.CommunicationException
     */
    public AuthIdentity getUser(String userName)
            throws CommunicationException {
        try {
            WebResource webResource = createRequestResource(
                    "users/" + userName);
            return webResource.get(AuthIdentity.class);
        } catch (UniformInterfaceException t) {
            throw new CommunicationException(
                    "Exception when executing GET user: " + userName, t);
        }
    }

    /**
     * Removes a the specified user from warp.
     *
     * @param userName the warp user name
     *
     * @throws CommunicationException in case communication with the warp
     * authentication manager fails
     */
    public void removeUser(String userName) throws CommunicationException {
        try {
            WebResource webResource = createRequestResource(
                    "users/" + userName);
            webResource.delete();
        } catch (UniformInterfaceException t) {
            throw new CommunicationException(
                    "Exception when executing DELETE user: " + userName, t);
        }
    }

    /**
     * Creates a user in Warp.
     *
     * @param warpUserCredentials user credentials
     *
     * @throws CommunicationException in case communication with the warp
     * authentication manager fails
     */
    public void createUser(AuthIdentity warpUserCredentials)
            throws CommunicationException {
        updateUser(warpUserCredentials.getUser(), warpUserCredentials);
    }

    /**
     * Updates the warp user with the specified name
     *
     * @param currentUserName the current warp user name
     * @param newUserCredentials new user credentials
     *
     * @throws CommunicationException in case communication with the warp
     * authentication manager fails
     */
    public void updateUser(
            String currentUserName,
            AuthIdentity newUserCredentials)
            throws CommunicationException {
        // make sure user name is not changed
        AuthIdentity userCred = new AuthIdentity(
                currentUserName, newUserCredentials.getPassword());
        try {
            WebResource webResource = createRequestResource(
                    "users/" + currentUserName);
            webResource.put(userCred);
        } catch (UniformInterfaceException t) {
            throw new CommunicationException(
                    "Exception when executing PUT user: " + newUserCredentials, t);
        }
    }

    private WebResource createRequestResource(String resource) {
        jerseyClient.removeAllFilters();
        String url = MessageFormat.format(authRequestFormat, warpServerAddr, resource);
        WebResource webResource;
        try {
            jerseyClient.addFilter(
                    new BasicAuthFilter(warpAdminUser, warpAdminPassword));
            final URI uri = new URI(url);
            webResource = jerseyClient.resource(uri);
            webResource.accept(MediaType.APPLICATION_JSON_TYPE);
        } catch (URISyntaxException e) {
            throw new Error("This is a coding error", e);
        }
        return webResource;
    }

    /**
     * Thrown in case communication with the warp authentication manager fails.
     */
    public static class CommunicationException extends IOException {

        public CommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
