package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.*;
import com.ericsson.deviceaccess.spi.GenericDeviceActivator;
import com.ericsson.deviceaccess.spi.event.EventManager;
import com.ericsson.research.common.testutil.ReflectionTestUtil;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static junit.framework.Assert.fail;


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
    private GenericDeviceServiceImpl service;
    private GenericDevicePropertyMetadata[] metadataArr;
    private GenericDevicePropertiesImpl props;
    private GenericDevicePropertyMetadata metadataString;
    private GenericDevicePropertyMetadata metadataInt;
    private GenericDevicePropertyMetadata metadataFloat;
    private GenericDeviceAction action;

    @Before
    public void setUp() throws Exception {
        action = context.mock(GenericDeviceAction.class);
        eventManager = context.mock(EventManager.class);
        device = context.mock(GenericDeviceImpl.class);
        metadataFloat = context.mock(GenericDevicePropertyMetadata.class, "metadataFloat");
        metadataInt = context.mock(GenericDevicePropertyMetadata.class, "metadataInt");
        metadataString = context.mock(GenericDevicePropertyMetadata.class, "metadataString");
        metadataArr = new GenericDevicePropertyMetadata[]{metadataFloat, metadataInt, metadataString};
        ReflectionTestUtil.setField(GenericDeviceActivator.class, "eventManager", eventManager);

        context.checking(new Expectations(){{
            allowing(device).getId();will(returnValue("devId"));
            allowing(device).getURN();will(returnValue("devUrn"));
            allowing(device).getName();will(returnValue("dev"));
            allowing(device).getProtocol();will(returnValue("prot"));
            allowing(device).isOnline();will(returnValue(true));

            allowing(metadataFloat).getDefaultNumberValue();will(returnValue(42.0f));
            allowing(metadataFloat).getDefaultStringValue();will(returnValue("42.0"));
            allowing(metadataFloat).getName();will(returnValue("fProp"));
            allowing(metadataFloat).getType();will(returnValue(Float.class));
            allowing(metadataFloat).getMinValue();will(returnValue(Float.NEGATIVE_INFINITY));
            allowing(metadataFloat).getMaxValue();will(returnValue(Float.POSITIVE_INFINITY));
            allowing(metadataFloat).serialize(GenericDevice.FORMAT_JSON);will(returnValue("{\"type\":\"float\"}"));

            allowing(metadataInt).getDefaultNumberValue();will(returnValue(42));
            allowing(metadataInt).getDefaultStringValue();will(returnValue("42"));
            allowing(metadataInt).getName();will(returnValue("iProp"));
            allowing(metadataInt).getType();will(returnValue(Integer.class));
            allowing(metadataInt).getMinValue();will(returnValue(Integer.MIN_VALUE));
            allowing(metadataInt).getMaxValue();will(returnValue(Integer.MAX_VALUE));
            allowing(metadataInt).serialize(GenericDevice.FORMAT_JSON);will(returnValue("{\"type\":\"int\"}"));

            allowing(metadataString).getDefaultStringValue();will(returnValue("Forty-two"));
            allowing(metadataString).getName();will(returnValue("sProp"));
            allowing(metadataString).getType();will(returnValue(String.class));
            allowing(metadataString).getValidValues();will(returnValue(null));
            allowing(metadataString).serialize(GenericDevice.FORMAT_JSON);will(returnValue("{\"type\":\"string\"}"));

            allowing(action).getName();will(returnValue("action1"));
            allowing(action).updatePath(with(aNonNull(String.class)));
        }});

        service = new GenericDeviceServiceImpl("srv", metadataArr);
        service.setParentDevice(device);
        service.putAction(action);

        props = new GenericDevicePropertiesImpl(metadataArr, service);
    }

    @After
    public void tearDown() throws Exception {
        ReflectionTestUtil.setField(GenericDeviceActivator.class, "eventManager", null);
    }

    @Test
    public void testFloatEvents() {
        context.checking(new Expectations(){{
            oneOf(device).notifyEvent(with("srv"), with(new Properties() {{
                put("fProp", 23.0f);
            }}));
        }});

        props.setFloatValue("fProp", 23);


        context.assertIsSatisfied();
    }

    @Test
    public void testIntEvents() {
        context.checking(new Expectations(){{
            oneOf(device).notifyEvent(with("srv"), with(new Properties() {{
                put("iProp", 23);
            }}));
        }});

        props.setIntValue("iProp", 23);


        context.assertIsSatisfied();
    }

    @Test
    public void testStringEvents() {
        context.checking(new Expectations(){{
            oneOf(device).notifyEvent(with("srv"), with(new Properties() {{
                put("sProp", "apa");
            }}));
        }});

        props.setStringValue("sProp", "apa");

        context.assertIsSatisfied();
    }

    @Test
    public void testSerialize() throws GenericDeviceException, JSONException {
        context.checking(new Expectations() {{
            oneOf(action).serialize(GenericDevice.FORMAT_JSON);
            will(returnValue("{\"name\":\"action1\"}"));
        }});
        String json = service.serialize(GenericDevice.FORMAT_JSON);

        context.assertIsSatisfied();
        // Just check that JSON parsing works
        try {
            JSONObject jsonObject = new JSONObject(json);
            System.out.println(jsonObject);
        } catch (Exception e) {
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
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        }
    }
}
