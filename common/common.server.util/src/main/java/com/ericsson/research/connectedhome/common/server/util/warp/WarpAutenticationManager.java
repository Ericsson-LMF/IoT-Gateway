/*
 * Copyright (c) Ericsson AB, 2011.
 *
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 *
 * ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ERICSSON SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.ericsson.research.connectedhome.common.server.util.warp;

import com.ericsson.research.connectedhome.common.BasicAuthFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.MessageFormat;

/**
 * Interface towards the warp authentication manager.
 * <p/>
 * Uses the web API:
 * <ul>
 * <li>GET /AuthenticationProviderView/api/users
 * <li>GET /AuthenticationProviderView/api/origin/{origin}
 * <li>GET /AuthenticationProviderView/api/users/{user}
 * <li>PUT /AuthenticationProviderView/api/users/{user}
 * <li>DELETE /AuthenticationProviderView/api/users/{user}
 * <ul>
 * Auth = Basic<br>
 * User = admin<br>
 * PW (default) = lamepass<br>
 * <p/>
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
     * @param jerseyClient      Jersey client to be used to access the AuthenticationProviderView
     * @param warpServerAddr    the host name/IP for the warp server
     * @param warpAdminUser     the admin user for AuthenticationProviderView
     * @param warpAdminPassword the admin password for AuthenticationProviderView
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
     * @throws com.ericsson.research.connectedhome.common.server.util.warp.WarpAutenticationManager.CommunicationException
     */
    public AuthIdentity getUser(String userName)
        throws CommunicationException {
        try {
            WebResource webResource = createRequestResource(
                "users/" + userName);
            return webResource.get(AuthIdentity.class);
        } catch (Exception t) {
            throw new CommunicationException(
                "Exception when executing GET user: " + userName, t);
        }
    }

    /**
     * Removes a the specified user from warp.
     *
     * @param userName the warp user name
     *
     * @throws CommunicationException in case communication with the warp authentication manager fails
     */
    public void removeUser(String userName) throws CommunicationException {
        try {
            WebResource webResource = createRequestResource(
                "users/" + userName);
            webResource.delete();
        } catch (Exception t) {
            throw new CommunicationException(
                "Exception when executing DELETE user: " + userName, t);
        }
    }

    /**
     * Creates a user in Warp.
     *
     * @param warpUserCredentials user credentials
     *
     * @throws CommunicationException in case communication with the warp authentication manager fails
     */
    public void createUser(AuthIdentity warpUserCredentials)
        throws CommunicationException {
        updateUser(warpUserCredentials.getUser(), warpUserCredentials);
    }

    /**
     * Updates the warp user with the specified name
     *
     * @param currentUserName    the current warp user name
     * @param newUserCredentials new user credentials
     *
     * @throws CommunicationException in case communication with the warp authentication manager fails
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
        } catch (Exception t) {
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
