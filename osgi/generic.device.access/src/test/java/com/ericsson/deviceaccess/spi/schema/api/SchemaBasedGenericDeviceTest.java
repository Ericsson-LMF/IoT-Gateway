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

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.spi.schema.*;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 */
public class SchemaBasedGenericDeviceTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };

    @Before
    public void setup() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testAddSchemaBasedService() {
        final SchemaBasedService service = context.mock(SchemaBasedService.class);

        context.checking(new Expectations() {
            {
                oneOf(service).setParentDevice(with(aNonNull(GenericDevice.class)));
                oneOf(service).getName();
                will(returnValue("myService"));
                oneOf(service).validateSchema();
                oneOf(service).updatePath("undefined/undefined");
            }
        });

        SchemaBasedGenericDevice device = new SchemaBasedGenericDevice() {
            {
                addSchemaBasedService(service);
            }
        };

        context.assertIsSatisfied();

        assertSame(service, device.getService("myService"));
    }

    @Test
    public void testErrors_addAlreadyExisting() {
        final SchemaBasedService service = context.mock(SchemaBasedService.class);

        context.checking(new Expectations() {
            {
                oneOf(service).setParentDevice(with(aNonNull(GenericDevice.class)));
                oneOf(service).getName();
                will(returnValue("alreadyExistingService"));
                oneOf(service).validateSchema();
                oneOf(service).updatePath("undefined/undefined");
                oneOf(service).getName();
                will(returnValue("alreadyExistingService"));
            }
        });

        try {
            SchemaBasedGenericDevice device = new SchemaBasedGenericDevice() {
                {
                    addSchemaBasedService(service);
                    addSchemaBasedService(service);
                }
            };
            fail("should have been exception here");
        } catch (ServiceSchemaError e) {
            // success
        }

        context.assertIsSatisfied();

    }

    @Test
    public void testCreateCustomService() {
        final ServiceSchema serviceSchema = context.mock(ServiceSchema.class);

        context.checking(new Expectations() {
            {
                oneOf(serviceSchema).getName();
                will(returnValue("myService"));
                oneOf(serviceSchema).getName();
                will(returnValue("myService"));
                oneOf(serviceSchema).getPropertiesSchemas();
                will(returnValue(new ParameterSchema[0]));
                oneOf(serviceSchema).getActionSchemas();
                will(returnValue(new ActionSchema[0]));
                oneOf(serviceSchema).getPropertiesSchemas();
                will(returnValue(new ParameterSchema[0]));
                oneOf(serviceSchema).getActionSchemas();
                will(returnValue(new ActionSchema[0]));
            }
        });

        SchemaBasedGenericDevice device = new SchemaBasedGenericDevice() {
            {
                addSchemaBasedService(createService(serviceSchema));
            }
        };

        context.assertIsSatisfied();

        assertEquals("myService", device.getService("myService").getName());
    }
}
