package com.ericsson.research.commonutil;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * StringUtil Tester.
 *
 */
public class StringUtilTest {
    /**
     *
     * Method: escapeAmpersand(String string)
     *
     */
    @Test 
    public void testEscapeAmpersand() {
        assertEquals("The &amp; has been escaped", StringUtil.escapeAmpersand("The & has been escaped"));
    }
}
