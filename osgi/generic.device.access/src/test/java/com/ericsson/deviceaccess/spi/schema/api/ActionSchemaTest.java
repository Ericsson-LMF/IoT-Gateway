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
import static org.junit.Assert.assertTrue;

import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;

/**
 * 
 */
public class ActionSchemaTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @Test
    public void test() {
        ParameterSchema a1 = context.mock(ParameterSchema.class, "a1");
        ParameterSchema a2 = context.mock(ParameterSchema.class, "a2");
        ParameterSchema r1 = context.mock(ParameterSchema.class, "r1");
        ParameterSchema r2 = context.mock(ParameterSchema.class, "r2");
        
        ActionSchema actionSchema = new ActionSchema.Builder("act1").
                setMandatory(true).
                addArgumentSchema(a1).
                addArgumentSchema(a2).
                addResultSchema(r1).
                addResultSchema(r2).
                build();
        
        
        assertEquals("act1", actionSchema.getName());
        assertTrue(actionSchema.isMandatory());
        assertArrayEquals(new ParameterSchema[]{a1, a2}, actionSchema.getArgumentsSchemas());
        assertArrayEquals(new ParameterSchema[]{r1, r2}, actionSchema.getResultSchema());
    }
}
