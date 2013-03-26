package com.ericsson.research.common.slf4jlogger;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * OSGiLogger Tester.
 */
public class OSGiLoggerTest {
    @Test
    public void testNoOsgiAvailable() {
        OSGILogFactory.initOSGI(null);
        Logger log = LoggerFactory.getLogger(OSGiLoggerTest.class);
        log.debug("test");
    }
}
