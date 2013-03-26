/**
 * Copyright (c) Ericsson AB, 2011.
 * <p/>
 * All Rights Reserved. Reproduction in whole or in part is prohibited without
 * the written consent of the copyright owner.
 * <p/>
 * ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE, OR
 * NON-INFRINGEMENT. ERICSSON SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY
 * LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING THIS SOFTWARE OR ITS
 * DERIVATIVES.
 */
package com.ericsson.research.connectedhome.common;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import org.apache.commons.codec.binary.Base64;

import javax.ws.rs.core.HttpHeaders;

/**
 * A filter to be used for basic HTTP authentication since the HTTPBasicAuthFilter
 * class has a bug.
 */
public final class BasicAuthFilter extends ClientFilter {

    private final String authentication;
    private final Base64 base64 = new Base64();


    /**
     * Creates a new HTTP Basic Authentication filter using provided user name
     * and password credentials.
     *
     * @param username
     * @param password
     */
    public BasicAuthFilter(final String username, final String password) {
        authentication =
            "Basic " + encodeCredentialsBasic(username, password);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ClientResponse handle(final ClientRequest cr) throws
        ClientHandlerException {

        if (!cr.getMetadata().containsKey(HttpHeaders.AUTHORIZATION)) {
            cr.getMetadata().add(HttpHeaders.AUTHORIZATION, authentication);
        }
        return getNext().handle(cr);
    }

    private String encodeCredentialsBasic(
        final String username, final String password) {

        String encode = username + ":" + password;
        return new String(base64.encode(encode.getBytes())).trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BasicAuthFilter that = (BasicAuthFilter) o;

        if (authentication != null ?
            !authentication.equals(that.authentication) :
            that.authentication != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return authentication != null ? authentication.hashCode() : 0;
    }
}
