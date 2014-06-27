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
package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.Serializable.Format;
import com.ericsson.deviceaccess.api.genericdevice.GDAction;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import com.ericsson.deviceaccess.spi.event.EventManager;
import com.ericsson.deviceaccess.spi.genericdevice.GDActivator;
import com.ericsson.deviceaccess.spi.impl.genericdevice.GDPropertiesImpl;
import com.ericsson.deviceaccess.spi.impl.genericdevice.GDServiceImpl;
import com.ericsson.research.common.testutil.ReflectionTestUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import junit.framework.Assert;
import static junit.framework.Assert.fail;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * GenericDeviceServiceImpl Tester.
 *
 */
public class GenericDeviceServiceImplTest {

    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private EventManager eventManager;
    private GenericDeviceImpl device;
    private GDServiceImpl service;
    private List<GDPropertyMetadata> metadataArr;
    private GDPropertiesImpl props;
    private GDPropertyMetadata metadataString;
    private GDPropertyMetadata metadataInt;
    private GDPropertyMetadata metadataFloat;
    private GDAction action;

    @Before
    public void setUp() throws Exception {
        action = context.mock(GDAction.class);
        eventManager = context.mock(EventManager.class);
        device = context.mock(GenericDeviceImpl.class);
        metadataFloat = context.mock(GDPropertyMetadata.class, "metadataFloat");
        metadataInt = context.mock(GDPropertyMetadata.class, "metadataInt");
        metadataString = context.mock(GDPropertyMetadata.class, "metadataString");
        metadataArr = new ArrayList<>();
        metadataArr.add(metadataFloat);
        metadataArr.add(metadataInt);
        metadataArr.add(metadataString);
        ReflectionTestUtil.setField(GDActivator.class, "eventManager", eventManager);

        context.checking(new Expectations() {
            {
                allowing(device).getId();
                will(returnValue("devId"));
                allowing(device).getURN();
                will(returnValue("devUrn"));
                allowing(device).getName();
                will(returnValue("dev"));
                allowing(device).getProtocol();
                will(returnValue("prot"));
                allowing(device).isOnline();
                will(returnValue(true));

                allowing(metadataFloat).getDefaultNumberValue();
                will(returnValue(42.0f));
                allowing(metadataFloat).getDefaultStringValue();
                will(returnValue("42.0"));
                allowing(metadataFloat).getName();
                will(returnValue("fProp"));
                allowing(metadataFloat).getType();
                will(returnValue(Float.class));
                allowing(metadataFloat).getTypeName();
                will(returnValue("Float"));
                allowing(metadataFloat).getValidValues();
                will(returnValue(null));
                allowing(metadataFloat).getMinValue();
                will(returnValue(Float.NEGATIVE_INFINITY));
                allowing(metadataFloat).getMaxValue();
                will(returnValue(Float.POSITIVE_INFINITY));
                allowing(metadataFloat).serialize(Format.JSON);
                will(returnValue("{\"type\":\"float\"}"));

                allowing(metadataInt).getDefaultNumberValue();
                will(returnValue(42));
                allowing(metadataInt).getDefaultStringValue();
                will(returnValue("42"));
                allowing(metadataInt).getName();
                will(returnValue("iProp"));
                allowing(metadataInt).getType();
                will(returnValue(Integer.class));
                allowing(metadataInt).getTypeName();
                will(returnValue("Integer"));
                allowing(metadataInt).getValidValues();
                will(returnValue(null));
                allowing(metadataInt).getMinValue();
                will(returnValue(Integer.MIN_VALUE));
                allowing(metadataInt).getMaxValue();
                will(returnValue(Integer.MAX_VALUE));
                allowing(metadataInt).serialize(Format.JSON);
                will(returnValue("{\"type\":\"int\"}"));

                allowing(metadataString).getDefaultNumberValue();
                will(returnValue(null));
                allowing(metadataString).getDefaultStringValue();
                will(returnValue("Forty-two"));
                allowing(metadataString).getName();
                will(returnValue("sProp"));
                allowing(metadataString).getType();
                will(returnValue(String.class));
                allowing(metadataString).getTypeName();
                will(returnValue("String"));
                allowing(metadataString).getValidValues();
                will(returnValue(null));
                allowing(metadataString).getMinValue();
                will(returnValue(null));
                allowing(metadataString).getMaxValue();
                will(returnValue(null));
                allowing(metadataString).serialize(Format.JSON);
                will(returnValue("{\"type\":\"string\"}"));

                allowing(action).getName();
                will(returnValue("action1"));
                allowing(action).updatePath(with(aNonNull(String.class)));
                allowing(action).getArgumentsMetadata();
                will(returnValue(new HashMap<>()));
                allowing(action).getResultMetadata();
                will(returnValue(new HashMap<>()));
            }
        });

        service = new GDServiceImpl("srv", metadataArr);
        service.setParentDevice(device);
        service.putAction(action);

        props = new GDPropertiesImpl(metadataArr, service);
    }

    @After
    public void tearDown() throws Exception {
        ReflectionTestUtil.setField(GDActivator.class, "eventManager", null);
    }

    @Test
    public void testFloatEvents() {
        context.checking(new Expectations() {
            {
                oneOf(device).notifyEvent(with("srv"), with(new HashMap() {
                    {
                        put("fProp", 23.0f);
                    }
                }));
            }
        });

        props.setFloatValue("fProp", 23);

        context.assertIsSatisfied();
    }

    @Test
    public void testIntEvents() {
        context.checking(new Expectations() {
            {
                oneOf(device).notifyEvent(with("srv"), with(new HashMap() {
                    {
                        put("iProp", 23);
                    }
                }));
            }
        });

        props.setIntValue("iProp", 23);

        context.assertIsSatisfied();
    }

    @Test
    public void testStringEvents() {
        context.checking(new Expectations() {
            {
                oneOf(device).notifyEvent(with("srv"), with(new HashMap() {
                    {
                        put("sProp", "apa");
                    }
                }));
            }
        });

        props.setStringValue("sProp", "apa");

        context.assertIsSatisfied();
    }

    @Test
    public void testSerialize() throws GDException, JSONException {
        context.checking(new Expectations() {
            {
                allowing(action).serialize(Format.JSON);
                will(returnValue("{\"name\":\"action1\"}"));
            }
        });
        String json = service.serialize(Format.JSON);
        System.out.println(json);

        context.assertIsSatisfied();
        // Just check that JSON parsing works
        try {
            JSONObject jsonObject = new JSONObject(json);
            System.out.println(jsonObject);
        } catch (JSONException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSerializeState() {
        String json = service.serializeState();
        System.out.println(json);

        context.assertIsSatisfied();
        // Just check that JSON parsing works
        try {
            JSONObject jsonObject = new JSONObject(json);
            System.out.println(jsonObject);
        } catch (JSONException e) {
            Assert.fail(e.getMessage());
        }
    }
}
