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

package com.ericsson.deviceaccess.spi.schema.api;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchemaError;

/**
 * 
 */
public class ParameterSchemaTest {

    @Test
    public void testNumber() {
        ParameterSchema parameterSchema = new ParameterSchema.Builder("par1").
                setType(Integer.class).
                setDefaultValue(new Integer(21)).
                setMaxValue("99").
                setMinValue("4").
                build();

        assertEquals("par1", parameterSchema.getName());
        assertSame(Integer.class, parameterSchema.getType());
        assertEquals("21", parameterSchema.getDefaultStringValue());
        assertEquals(21, parameterSchema.getDefaultNumberValue().intValue());
        assertEquals(99, parameterSchema.getMaxValue().intValue());
        assertEquals(4, parameterSchema.getMinValue().intValue());
    }

    @Test
    public void testString() {
        ParameterSchema parameterSchema = new ParameterSchema.Builder("par1").
                setType(String.class).
                setDefaultValue("banan").
                setValidValues(new String[] { "orange", "banan" }).
                build();

        assertEquals("par1", parameterSchema.getName());
        assertSame(String.class, parameterSchema.getType());
        assertEquals("banan", parameterSchema.getDefaultStringValue());
        assertArrayEquals(new String[] { "banan", "orange" }, parameterSchema.getValidValues());
    }

    @Test
    public void testWrongDefault() {
        try {
            ParameterSchema parameterSchema = new ParameterSchema.Builder("par1").
                    setType(String.class).
                    setDefaultValue(new Integer(2)).
                    build();
            fail("should cause exception");
        } catch (ServiceSchemaError e) {
            // success
        }
    }
}
