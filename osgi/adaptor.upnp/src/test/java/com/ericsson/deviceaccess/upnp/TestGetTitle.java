package com.ericsson.deviceaccess.upnp;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestGetTitle {

    @Test
    public void testGetTitleWithPartlyEncoded() {
        String title = UPnPDeviceAgent.getMediaTitle("item>&lt;dc:title>EXPECTED TITLE&lt;/dc:title>&lt;upnp:class>");
        assertEquals("EXPECTED TITLE", title);
    }

    @Test
    public void testGetTitleWithFullyEncoded() {
        String title = UPnPDeviceAgent.getMediaTitle("&lt;dc:title&gt;EXPECTED TITLE&lt;/dc:title&gt;&lt;");
        assertEquals("EXPECTED TITLE", title);
    }

    @Test
    public void testGetTitleNotEncoded() {
        String title = UPnPDeviceAgent.getMediaTitle("<dc:title>EXPECTED TITLE</dc:title>");
        assertEquals("EXPECTED TITLE", title);
    }
}
