package com.ericsson.research.connectedhome.common;

import com.sun.jersey.api.client.ClientHandler;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.core.HttpHeaders;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BasicAuthFilterTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private ClientRequest clientRequest;
    private MultivaluedMapImpl headers;
    private BasicAuthFilter authFilter;
    private ClientHandler dummy;

    @Before
    public void setup() {
        dummy = context.mock(ClientHandler.class);
        authFilter = new BasicAuthFilter("admin", "lamepass");
        ReflectionTestUtils.setField(authFilter, "next", dummy);
        clientRequest = context.mock(ClientRequest.class);

        headers = new MultivaluedMapImpl();

        context.checking(new Expectations() {{
            allowing(clientRequest).getMetadata();
            will(returnValue(headers));
            allowing(dummy).handle(with(aNonNull(ClientRequest.class)));
            will(returnValue(null));
        }});
    }

    @Test
    public void test() {
        authFilter.handle(clientRequest);

        assertTrue(""+headers.get(HttpHeaders.AUTHORIZATION), headers.get(HttpHeaders.AUTHORIZATION).contains(
            "Basic YWRtaW46bGFtZXBhc3M="));
    }

    @Test
    public void testAuthAlreadySet() {
        headers.add(HttpHeaders.AUTHORIZATION, "banan");

        authFilter.handle(clientRequest);

        assertTrue(!headers.get(HttpHeaders.AUTHORIZATION).contains(
            "Basic YWRtaW46bGFtZXBhc3M="));
    }

    @Test
    public void testEqualsAndHashCode() {
        BasicAuthFilter authFilter2 = new BasicAuthFilter("admin", "lamepass");
        assertEquals(authFilter, authFilter2);
        assertEquals(authFilter.hashCode(), authFilter2.hashCode());
    }
}
