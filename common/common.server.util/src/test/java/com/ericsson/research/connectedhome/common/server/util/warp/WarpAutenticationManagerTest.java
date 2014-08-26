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
import static com.ericsson.research.connectedhome.common.Matchers.anAuthIdentity;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import java.net.URI;
import javax.ws.rs.core.MediaType;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class WarpAutenticationManagerTest {

    private JUnit4Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private WarpAutenticationManager warpAutenticationManager;
    private Client client;
    private WebResource resource;
    private UniformInterfaceException expectedException;

    @Before
    public void setup() throws Exception {
        client = context.mock(Client.class);
        resource = context.mock(WebResource.class);
        expectedException = context.mock(UniformInterfaceException.class);
        context.checking(new Expectations() {
            {
                allowing(expectedException).fillInStackTrace();
                will(returnValue(expectedException));
            }
        });
        warpAutenticationManager = new WarpAutenticationManager(client, "192.121.150.61:8080", "admin", "lamepass");
    }

    @After
    public void tearDown() throws Exception {
    }

//    public void test1() throws WarpAutenticationManager.CommunicationException {
//        ReflectionTestUtils.setField(
//            warpAutenticationManager, "jerseyClient", new Client());
//
//
//        warpAutenticationManager.createUser(new AuthIdentity("joel", "joel"));
//
//        AuthIdentity user = warpAutenticationManager.getUser("joel");
//        System.out.println("User: "+user);
//
//        warpAutenticationManager.updateUser("joel", new AuthIdentity("joel", "banan"));
//
//        user = warpAutenticationManager.getUser("joel");
//        System.out.println("User: "+user);
//
//        warpAutenticationManager.removeUser("joel");
//
//        try {
//            warpAutenticationManager.getUser("joel");
//            fail("Should have been exception here");
//        } catch (WarpAutenticationManager.CommunicationException e) {
//            // As expected
//        }
//
//    }
    @Test
    public void testGetUser() throws Exception {
        context.checking(
                new Expectations() {
                    {
                        oneOf(client).removeAllFilters();
                        oneOf(client).addFilter(
                                with(equal(new BasicAuthFilter("admin", "lamepass"))));
                        oneOf(client).resource(
                                new URI(
                                        "http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                        will(returnValue(resource));
                        oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                        oneOf(resource).get(AuthIdentity.class);
                        will(returnValue(new AuthIdentity("joe", "joe")));
                    }
                });

        AuthIdentity user = warpAutenticationManager.getUser("joe");

        context.assertIsSatisfied();

        assertEquals("joe", user.getUser());
    }

    @Test
    public void testGetUserError() throws Exception {
        context.checking(
                new Expectations() {
                    {
                        oneOf(client).removeAllFilters();
                        oneOf(client).addFilter(
                                with(equal(new BasicAuthFilter("admin", "lamepass"))));
                        oneOf(client).resource(
                                new URI("http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                        will(returnValue(resource));
                        oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                        oneOf(resource).get(AuthIdentity.class);
                        will(throwException(expectedException));
                    }
                });

        try {
            warpAutenticationManager.getUser("joe");
            fail("Expected an exception here");
        } catch (WarpAutenticationManager.CommunicationException e) {
            // success
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testGetUserError2() throws Exception {
        context.checking(
                new Expectations() {
                    {
                        oneOf(client).removeAllFilters();
                        oneOf(client).addFilter(
                                with(equal(new BasicAuthFilter("admin", "lamepass"))));
                    }
                });

        try {
            warpAutenticationManager.getUser(" ");
            fail("Expected an exception here");
        } catch (Error e) {
            // success
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoveUser() throws Exception {
        context.checking(
                new Expectations() {
                    {
                        oneOf(client).removeAllFilters();
                        oneOf(client).addFilter(
                                with(equal(new BasicAuthFilter("admin", "lamepass"))));
                        oneOf(client).resource(
                                new URI("http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                        will(returnValue(resource));
                        oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                        oneOf(resource).delete();
                    }
                });

        warpAutenticationManager.removeUser("joe");

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoveUserError() throws Exception {
        context.checking(
                new Expectations() {
                    {
                        oneOf(client).removeAllFilters();
                        oneOf(client).addFilter(
                                with(equal(new BasicAuthFilter("admin", "lamepass"))));
                        oneOf(client).resource(
                                new URI("http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                        will(returnValue(resource));
                        oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                        oneOf(resource).delete();
                        will(throwException(expectedException));
                    }
                });

        try {
            warpAutenticationManager.removeUser("joe");
            fail("Expected an exception here");
        } catch (WarpAutenticationManager.CommunicationException e) {
            // success
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateUser() throws Exception {
        final AuthIdentity authIdentity = new AuthIdentity("joe", "joe");
        context.checking(
                new Expectations() {
                    {
                        oneOf(client).removeAllFilters();
                        oneOf(client).addFilter(
                                with(equal(new BasicAuthFilter("admin", "lamepass"))));
                        oneOf(client).resource(
                                new URI("http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                        will(returnValue(resource));
                        oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                        oneOf(resource).put(with(anAuthIdentity(authIdentity)));
                    }
                });

        warpAutenticationManager.createUser(authIdentity);

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateOrUpdateUserError() throws Exception {
        final AuthIdentity authIdentity = new AuthIdentity("joe", "joe");
        context.checking(
                new Expectations() {
                    {
                        oneOf(client).removeAllFilters();
                        oneOf(client).addFilter(
                                with(equal(new BasicAuthFilter("admin", "lamepass"))));
                        oneOf(client).resource(
                                new URI("http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                        will(returnValue(resource));
                        oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                        oneOf(resource).put(with(anAuthIdentity(authIdentity)));
                        will(throwException(expectedException));
                    }
                });

        try {
            warpAutenticationManager.createUser(authIdentity);
            fail("Expected an exception here");
        } catch (WarpAutenticationManager.CommunicationException e) {
            // success
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testUpdateUser() throws Exception {
        final AuthIdentity authIdentity = new AuthIdentity("joe", "banan");
        context.checking(
                new Expectations() {
                    {
                        oneOf(client).removeAllFilters();
                        oneOf(client).addFilter(
                                with(equal(new BasicAuthFilter("admin", "lamepass"))));
                        oneOf(client).resource(
                                new URI("http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                        will(returnValue(resource));
                        oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                        oneOf(resource).put(with(anAuthIdentity(authIdentity)));
                    }
                });

        warpAutenticationManager.updateUser("joe", authIdentity);

        context.assertIsSatisfied();
    }
}
