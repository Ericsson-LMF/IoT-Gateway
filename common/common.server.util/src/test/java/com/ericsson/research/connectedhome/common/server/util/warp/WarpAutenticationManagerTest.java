package com.ericsson.research.connectedhome.common.server.util.warp;

import com.ericsson.research.connectedhome.common.BasicAuthFilter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.net.URI;

import static com.ericsson.research.connectedhome.common.Matchers.*;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class WarpAutenticationManagerTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private WarpAutenticationManager warpAutenticationManager;
    private Client client;
    private WebResource resource;

    @Before
    public void setup() throws Exception {
        client = context.mock(Client.class);
        resource = context.mock(WebResource.class);
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
            new Expectations() {{
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
            }});

        AuthIdentity user = warpAutenticationManager.getUser("joe");

        context.assertIsSatisfied();

        assertEquals("joe", user.getUser());
    }

    @Test
    public void testGetUserError() throws Exception {
        context.checking(
            new Expectations() {{
                oneOf(client).removeAllFilters();
                oneOf(client).addFilter(
                    with(equal(new BasicAuthFilter("admin", "lamepass"))));
                oneOf(client).resource(
                    new URI(
                        "http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                will(returnValue(resource));
                oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                oneOf(resource).get(AuthIdentity.class);
                will(
                    throwException(new IOException("Expected test exception")));
            }});


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
            new Expectations() {{
                oneOf(client).removeAllFilters();
                oneOf(client).addFilter(
                    with(equal(new BasicAuthFilter("admin", "lamepass"))));
            }});


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
            new Expectations() {{
                oneOf(client).removeAllFilters();
                oneOf(client).addFilter(
                    with(equal(new BasicAuthFilter("admin", "lamepass"))));
                oneOf(client).resource(
                    new URI(
                        "http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                will(returnValue(resource));
                oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                oneOf(resource).delete();
            }});

        warpAutenticationManager.removeUser("joe");

        context.assertIsSatisfied();
    }

    @Test
    public void testRemoveUserError() throws Exception {
        context.checking(
            new Expectations() {{
                oneOf(client).removeAllFilters();
                oneOf(client).addFilter(
                    with(equal(new BasicAuthFilter("admin", "lamepass"))));
                oneOf(client).resource(
                    new URI(
                        "http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                will(returnValue(resource));
                oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                oneOf(resource).delete();
                will(
                    throwException(new IOException("Expected test exception")));
            }});

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
            new Expectations() {{
                oneOf(client).removeAllFilters();
                oneOf(client).addFilter(
                    with(equal(new BasicAuthFilter("admin", "lamepass"))));
                oneOf(client).resource(
                    new URI(
                        "http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                will(returnValue(resource));
                oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                oneOf(resource).put(with(anAuthIdentity(authIdentity)));
            }});

        warpAutenticationManager.createUser(authIdentity);

        context.assertIsSatisfied();
    }

    @Test
    public void testCreateOrUpdateUserError() throws Exception {
        final AuthIdentity authIdentity = new AuthIdentity("joe", "joe");
        context.checking(
            new Expectations() {{
                oneOf(client).removeAllFilters();
                oneOf(client).addFilter(
                    with(equal(new BasicAuthFilter("admin", "lamepass"))));
                oneOf(client).resource(
                    new URI(
                        "http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                will(returnValue(resource));
                oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                oneOf(resource).put(with(anAuthIdentity(authIdentity)));
                will(
                    throwException(new IOException("Expected test exception")));
            }});

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
            new Expectations() {{
                oneOf(client).removeAllFilters();
                oneOf(client).addFilter(
                    with(equal(new BasicAuthFilter("admin", "lamepass"))));
                oneOf(client).resource(
                    new URI(
                        "http://192.121.150.61:8080/Core-Authentication-View/api/users/joe"));
                will(returnValue(resource));
                oneOf(resource).accept(MediaType.APPLICATION_JSON_TYPE);
                oneOf(resource).put(with(anAuthIdentity(authIdentity)));
            }});

        warpAutenticationManager.updateUser("joe", authIdentity);

        context.assertIsSatisfied();
    }
}
