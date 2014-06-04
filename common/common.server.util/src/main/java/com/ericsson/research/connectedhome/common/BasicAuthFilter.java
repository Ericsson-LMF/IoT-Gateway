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
package com.ericsson.research.connectedhome.common;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.filter.ClientFilter;
import javax.ws.rs.core.HttpHeaders;
import org.apache.commons.codec.binary.Base64;

/**
 * A filter to be used for basic HTTP authentication since the
 * HTTPBasicAuthFilter class has a bug.
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
        authentication
                = "Basic " + encodeCredentialsBasic(username, password);
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        BasicAuthFilter that = (BasicAuthFilter) o;

        if (authentication != null
                ? !authentication.equals(that.authentication)
                : that.authentication != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return authentication != null ? authentication.hashCode() : 0;
    }
}
