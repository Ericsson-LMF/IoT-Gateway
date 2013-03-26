package com.ericsson.research.connectedhome.common.server.util.warp;


import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.springframework.test.util.ReflectionTestUtils;

public class AuthIdentityTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @Test
    public void testGetUser() throws Exception {
        assertEquals("joe", new AuthIdentity("joe", "pw").getUser());
    }

    @Test
    public void testSetUser() throws Exception {
        final AuthIdentity authIdentity = new AuthIdentity("joe", "pw");
        authIdentity.setUser("banan");
        assertEquals("banan", authIdentity.getUser());
    }

    @Test
    public void testGetPassword() throws Exception {
        assertEquals("pw", new AuthIdentity("joe", "pw").getPassword());
    }

    @Test
    public void testSetPassword() throws Exception {
        final AuthIdentity authIdentity = new AuthIdentity("joe", "pw");
        authIdentity.setPassword("banan");
        assertEquals("banan", authIdentity.getPassword());
    }

    @Test
    public void testGetOrigin() throws Exception {
        assertEquals("dlnaserver", new AuthIdentity("joe", "pw").getOrigin());
    }

    @Test
    public void testSetOrigin() throws Exception {
        final AuthIdentity authIdentity = new AuthIdentity("joe", "pw");
        authIdentity.setOrigin("banan");
        assertEquals("banan", authIdentity.getOrigin());
    }

    @Test
    public void testToString() throws Exception {
        assertNotNull(new AuthIdentity().toString());
    }
}
