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

import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Test;

import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchema;

/**
 * 
 */
public class ServiceSchemaTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };


    @Test
    public void test() {
        ActionSchema action1 = context.mock(ActionSchema.class, "action1");
        ActionSchema action2 = context.mock(ActionSchema.class, "action2");
        ParameterSchema property1 = context.mock(ParameterSchema.class, "prop1");
        ParameterSchema property2 = context.mock(ParameterSchema.class, "prop2");

        ServiceSchema serviceSchema = new ServiceSchema.Builder("service").
                addActionSchema(action1).
                addActionSchema(action2).
                addPropertySchema(property1).
                addPropertySchema(property2).
                build();
        
        assertArrayEquals(new ActionSchema[]{action1, action2}, serviceSchema.getActionSchemas());
        assertArrayEquals(new ParameterSchema[]{property1, property2}, serviceSchema.getPropertiesSchemas());
        assertEquals("service", serviceSchema.getName());
    }

}
