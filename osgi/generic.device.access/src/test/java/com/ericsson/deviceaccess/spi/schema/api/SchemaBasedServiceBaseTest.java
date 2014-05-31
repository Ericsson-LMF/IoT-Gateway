/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 * 
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,
 
 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 
 * 
 */
package com.ericsson.deviceaccess.spi.schema.api;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.spi.GenericDeviceActivator;
import com.ericsson.deviceaccess.spi.event.EventManager;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceImpl;
import com.ericsson.deviceaccess.spi.schema.*;
import com.ericsson.research.common.testutil.ReflectionTestUtil;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Dictionary;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 *
 */
public class SchemaBasedServiceBaseTest {

    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private ServiceSchema serviceSchema;
    private ActionSchema actionSchema;
    private ParameterSchema ParameterSchemaImpl1;
    private ParameterSchema ParameterSchemaImpl2;
    private GenericDeviceImpl device;
    private SchemaBasedServiceBase service;

    @Before
    public void setup() {
        device = context.mock(GenericDeviceImpl.class);
        context.checking(new Expectations() {
            {
                allowing(device).getId();
                will(returnValue("device"));
            }
        });

        serviceSchema = new ServiceSchema.Builder("@@TEST@@").
                addActionSchema(new ActionSchema.Builder("action").
                        setMandatory(true).
                        addArgumentSchema(new ParameterSchema.Builder("arg", Integer.class).
                                setMinValue("-10").
                                setMaxValue("10").
                                build()).
                        addArgumentSchema(new ParameterSchema.Builder("arg2", Integer.class).
                                setMinValue("-10").
                                setMaxValue("10").
                                build()).
                        addResultSchema(new ParameterSchema.Builder("res1", Integer.class).
                                build()).
                        build()).
                addActionSchema(new ActionSchema.Builder("optionalAction").build()).
                addPropertySchema(new ParameterSchema.Builder("prop1", Integer.class).build()).
                build();
        actionSchema = context.mock(ActionSchema.class);
        ParameterSchemaImpl1 = context.mock(ParameterSchema.class, "ParameterSchemaImpl1");
        ParameterSchemaImpl2 = context.mock(ParameterSchema.class, "ParameterSchemaImpl2");
        service = new SchemaBasedServiceBase(serviceSchema);
        ReflectionTestUtil.setField(service, "parentDevice", device);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testServiceWithProperty() throws GenericDeviceException {
        SchemaBasedService service = new SchemaBasedServiceBase(serviceSchema);
        assertEquals("prop1", service.getProperties().getNames()[0]);
    }

    @Test
    public void testDefineAction() throws GenericDeviceException {

        final int[] ctr = new int[1];
        service.defineAction("action", context1 -> ctr[0]++);

        service.getAction("action").execute((GenericDeviceProperties) null);
        assertEquals(1, ctr[0]);
    }

    @Test
    public void testDefineAction_noActionSchema() {
        try {
            service.defineAction("notInSchema", context1 -> {
            });
            fail("should have been exception here");
        } catch (ServiceSchemaError e) {
            // success
        }
    }

    @Test
    public void testDefineAction_alreadyDefined() {
        try {
            service.defineAction("action", context1 -> {
            });
            service.defineAction("action", context1 -> {
            });
            fail("should have been exception here");
        } catch (ServiceSchemaError e) {
            // success
        }
    }

    @Test
    public void testDefineCustomAction() throws GenericDeviceException {
        context.checking(new Expectations() {
            {
                // define custom action
                oneOf(actionSchema).getName();
                will(returnValue("customAction"));
                oneOf(actionSchema).getName();
                will(returnValue("customAction"));
                oneOf(actionSchema).getResultSchema();
                will(returnValue(new ParameterSchema[0]));
                oneOf(actionSchema).getArgumentsSchemas();
                will(returnValue(new ParameterSchema[0]));
            }
        });

        SchemaBasedService service = new SchemaBasedServiceBase(serviceSchema);
        ReflectionTestUtil.setField(service, "parentDevice", device);
        final int[] ctr = new int[1];
        service.defineCustomAction(actionSchema, context1 -> ctr[0]++);

        service.getAction("customAction").execute((GenericDeviceProperties) null);

        context.assertIsSatisfied();
        assertEquals(1, ctr[0]);
    }

    @Test
    public void testDefineCustomAction_alreadyInServiceSchemaImpl() throws GenericDeviceException {
        context.checking(new Expectations() {
            {
                // define custom action
                oneOf(actionSchema).getName();
                will(returnValue("action"));
            }
        });

        SchemaBasedService service = new SchemaBasedServiceBase(serviceSchema);
        ReflectionTestUtil.setField(service, "parentDevice", device);
        try {
            service.defineCustomAction(actionSchema, context1 -> {
            });
            fail("should have been exception here");
        } catch (ServiceSchemaError e) {
            // success
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testValidateSchema_ok() {
        final int[] ctr = new int[1];
        service.defineAction("action", context1 -> ctr[0]++);

        try {
            service.validateSchema();
        } catch (ServiceSchemaError e) {
            fail("Should not cause exception");
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testValidateSchema_error() {

        try {
            service.validateSchema();
            fail("Should cause exception");
        } catch (ServiceSchemaError e) {
            // success
        }

        context.assertIsSatisfied();
    }

    @Test
    public void testDefineDynamicProperty() throws GenericDeviceException {
        final EventManager eventManager = context.mock(EventManager.class);
        ReflectionTestUtil.setField(GenericDeviceActivator.class, "eventManager", eventManager);
        context.checking(new Expectations() {
            {
                oneOf(device).notifyEvent("@@TEST@@", new Properties() {
                    {
                        put("dyn", 42);
                    }
                });
            }
        });

        SchemaBasedService service = new SchemaBasedServiceBase(serviceSchema) {
            {
                addDynamicProperty(new ParameterSchema.Builder("dyn", Integer.class).build());
            }
        };
        ReflectionTestUtil.setField(service, "parentDevice", device);
        service.getProperties().setIntValue("dyn", 42);

        context.assertIsSatisfied();
        assertEquals(42, service.getProperties().getIntValue("dyn"));
    }

    @Test
    public void testRefreshProperties() throws GenericDeviceException {
        final EventManager eventManager = context.mock(EventManager.class);
        ReflectionTestUtil.setField(GenericDeviceActivator.class, "eventManager", eventManager);
        context.checking(new Expectations() {
            {
                allowing(eventManager).addEvent(with(any(String.class)), with(any(String.class)), with(any(Dictionary.class)));
            }
        });

        context.checking(new Expectations() {
            {
                // define custom action
                allowing(device).getId();
                will(returnValue("id"));
                allowing(device).getProtocol();
                will(returnValue("protocol"));
                allowing(device).isOnline();
                will(returnValue(true));
                allowing(device).getURN();
                will(returnValue("urn"));
                allowing(device).getName();
                will(returnValue("name"));
            }
        });

        ReflectionTestUtil.setField(service, "parentDevice", device);

        final boolean[] actionDone = new boolean[1];
        SchemaBasedService service = new SchemaBasedServiceBase(serviceSchema) {
            {
                defineAction(REFRESH_PROPERTIES, context1 -> actionDone[0] = true);
                defineAction("action", context -> {
                });
            }
        };
        service.validateSchema();
        service.getAction(SchemaBasedServiceBase.REFRESH_PROPERTIES).execute((GenericDeviceProperties) null);

        context.assertIsSatisfied();
        assertEquals(true, actionDone[0]);
    }
}
