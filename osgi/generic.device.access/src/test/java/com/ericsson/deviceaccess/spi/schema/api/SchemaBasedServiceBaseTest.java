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

import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.spi.event.EventManager;
import com.ericsson.deviceaccess.spi.genericdevice.GDActivator;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceImpl;
import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchemaError;
import com.ericsson.deviceaccess.spi.schema.based.SBService;
import com.ericsson.deviceaccess.spi.schema.based.SBServiceBase;
import com.ericsson.research.common.testutil.ReflectionTestUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

/**
 *
 */
public class SchemaBasedServiceBaseTest {

    private JUnit4Mockery context = new JUnit4Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private ServiceSchema serviceSchema;
    private ActionSchema actionSchema;
    private ParameterSchema ParameterSchemaImpl1;
    private ParameterSchema ParameterSchemaImpl2;
    private GenericDeviceImpl device;
    private SBServiceBase service;

    @Before
    public void setup() {
        device = context.mock(GenericDeviceImpl.class);
        context.checking(new Expectations() {
            {
                allowing(device).getId();
                will(returnValue("device"));
            }
        });

        serviceSchema = new ServiceSchema.Builder().setName("@@TEST@@")
                .addAction(a -> {
                    a.setName("action");
                    a.setMandatory(true);
                    a.addArgument(p -> {
                        p.setName("arg");
                        p.setType(Integer.class);
                        p.setMinValue("-10");
                        p.setMaxValue("10");
                    });
                    a.addArgument(p -> {
                        p.setName("arg2");
                        p.setType(Integer.class);
                        p.setMinValue("-10");
                        p.setMaxValue("10");
                    });
                    a.addResult("res1", Integer.class);
                })
                .addAction("optionalAction")
                .addProperty("prop1", Integer.class)
                .build();
        actionSchema = context.mock(ActionSchema.class);
        ParameterSchemaImpl1 = context.mock(ParameterSchema.class, "ParameterSchemaImpl1");
        ParameterSchemaImpl2 = context.mock(ParameterSchema.class, "ParameterSchemaImpl2");
        service = new SBServiceBase(serviceSchema);
        ReflectionTestUtil.setField(service, "parentDevice", device);
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testServiceWithProperty() throws GDException {
        SBService service = new SBServiceBase(serviceSchema);
        assertEquals("prop1", service.getProperties().getProperties().keySet().toArray()[0]);
    }

    @Test
    public void testDefineAction() throws GDException {

        final int[] ctr = new int[1];
        service.defineAction("action", context1 -> ctr[0]++);

        service.getAction("action").execute((GDProperties) null);
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
    public void testDefineCustomAction() throws GDException {
        context.checking(new Expectations() {
            {
                // define custom action
                oneOf(actionSchema).getName();
                will(returnValue("customAction"));
                oneOf(actionSchema).getName();
                will(returnValue("customAction"));
                oneOf(actionSchema).getResultSchema();
                will(returnValue(new ArrayList<>()));
                oneOf(actionSchema).getArgumentsSchemas();
                will(returnValue(new ArrayList<>()));
            }
        });

        SBService service = new SBServiceBase(serviceSchema);
        ReflectionTestUtil.setField(service, "parentDevice", device);
        final int[] ctr = new int[1];
        service.defineCustomAction(actionSchema, context1 -> ctr[0]++);

        service.getAction("customAction").execute((GDProperties) null);

        context.assertIsSatisfied();
        assertEquals(1, ctr[0]);
    }

    @Test
    public void testDefineCustomAction_alreadyInServiceSchemaImpl() throws GDException {
        context.checking(new Expectations() {
            {
                // define custom action
                oneOf(actionSchema).getName();
                will(returnValue("action"));
            }
        });

        SBService service = new SBServiceBase(serviceSchema);
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
    public void testDefineDynamicProperty() throws GDException {
        final EventManager eventManager = context.mock(EventManager.class);
        ReflectionTestUtil.setField(GDActivator.class, "eventManager", eventManager);
        context.checking(new Expectations() {
            {
                oneOf(device).notifyEvent("@@TEST@@", new HashMap() {
                    {
                        put("dyn", 42);
                    }
                });
            }
        });

        SBService service = new SBServiceBase(serviceSchema) {
            {
                addDynamicProperty(new ParameterSchema.Builder().setName("dyn").setType(Integer.class).build());
            }
        };
        ReflectionTestUtil.setField(service, "parentDevice", device);
        service.getProperties().setIntValue("dyn", 42);

        context.assertIsSatisfied();
        assertEquals(42, service.getProperties().getIntValue("dyn"));
    }

    @Test
    public void testRefreshProperties() throws GDException {
        final EventManager eventManager = context.mock(EventManager.class);
        ReflectionTestUtil.setField(GDActivator.class, "eventManager", eventManager);
        context.checking(new Expectations() {
            {
                allowing(eventManager).addPropertyEvent(with(any(String.class)), with(any(String.class)), with(any(Map.class)));
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
        SBService service = new SBServiceBase(serviceSchema) {
            {
                defineAction(REFRESH_PROPERTIES, context1 -> actionDone[0] = true);
                defineAction("action", context -> {
                });
            }
        };
        service.validateSchema();
        service.getAction(SBServiceBase.REFRESH_PROPERTIES).execute((GDProperties) null);

        context.assertIsSatisfied();
        assertEquals(true, actionDone[0]);
    }
}
