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

import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
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
        context.checking(new Expectations() {{
            allowing(device).getId();
            will(returnValue("device"));
        }});

        serviceSchema = new ServiceSchema.Builder("@@TEST@@").
                addActionSchema(new ActionSchema.Builder("action").
                        setMandatory(true).
                        addArgumentSchema(new ParameterSchema.Builder("arg").
                                setType(Integer.class).
                                setDefaultValue(new Integer(0)).
                                setMinValue("-10").
                                setMaxValue("10").
                                build()).
                        addArgumentSchema(new ParameterSchema.Builder("arg2").
                                setType(Integer.class).
                                setDefaultValue(new Integer(0)).
                                setMinValue("-10").
                                setMaxValue("10").
                                build()).
                        addResultSchema(new ParameterSchema.Builder("res1").
                                setType(Integer.class).
                                setDefaultValue(new Integer(0)).
                                build()).
                        build()).
                addActionSchema(new ActionSchema.Builder("optionalAction").build()).
                addPropertySchema(new ParameterSchema.Builder("prop1").setType(Integer.class).setDefaultValue(new Integer(0)).build()).
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
        service.defineAction("action", new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                ctr[0]++;
            }
        });

        service.getAction("action").execute((GenericDeviceProperties) null);
        assertEquals(1, ctr[0]);
    }

    @Test
    public void testDefineAction_noActionSchema() {
        try {
            service.defineAction("notInSchema", new ActionDefinition() {
                public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                }
            });
            fail("should have been exception here");
        } catch (ServiceSchemaError e) {
            // success
        }
    }

    @Test
    public void testDefineAction_alreadyDefined() {
        try {
            service.defineAction("action", new ActionDefinition() {
                public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                }
            });
            service.defineAction("action", new ActionDefinition() {
                public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                }
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
        service.defineCustomAction(actionSchema, new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                ctr[0]++;
            }
        });

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
            service.defineCustomAction(actionSchema, new ActionDefinition() {
                public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                }
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
        service.defineAction("action", new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                ctr[0]++;
            }
        });

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
        context.checking(new Expectations() {{
            oneOf(device).notifyEvent("@@TEST@@", new Properties() {{
                put("dyn", 42);
            }});
        }});

        SchemaBasedService service = new SchemaBasedServiceBase(serviceSchema) {{
            addDynamicProperty(new ParameterSchema.Builder("dyn").setType(Integer.class).build());
        }};
        ReflectionTestUtil.setField(service, "parentDevice", device);
        service.getProperties().setIntValue("dyn", 42);

        context.assertIsSatisfied();
        assertEquals(42, service.getProperties().getIntValue("dyn"));
    }

    @Test
    public void testRefreshProperties() throws GenericDeviceException {
        final EventManager eventManager = context.mock(EventManager.class);
        ReflectionTestUtil.setField(GenericDeviceActivator.class, "eventManager", eventManager);
        context.checking(new Expectations() {{
            allowing(eventManager).notifyGenericDeviceEvent(with(any(String.class)), with(any(String.class)), with(any(Dictionary.class)));
        }});

        context.checking(new Expectations() {
            {
                // define custom action
                allowing(device).getId();will(returnValue("id"));
                allowing(device).getProtocol();will(returnValue("protocol"));
                allowing(device).isOnline();will(returnValue(true));
                allowing(device).getURN();will(returnValue("urn"));
                allowing(device).getName();will(returnValue("name"));
            }
        });

        ReflectionTestUtil.setField(service, "parentDevice", device);

        final boolean[] actionDone = new boolean[1];
        SchemaBasedService service = new SchemaBasedServiceBase(serviceSchema) {{
            defineAction(REFRESH_PROPERTIES, new ActionDefinition() {
                public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                    actionDone[0] = true;
                }
            });
            defineAction("action", new ActionDefinition() {
                public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                    // needed since schema says it is mandatory
                }
            });
        }};
        service.validateSchema();
        service.getAction(SchemaBasedServiceBase.REFRESH_PROPERTIES).execute((GenericDeviceProperties) null);

        context.assertIsSatisfied();
        assertEquals(true, actionDone[0]);
    }
}
