package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDevicePropertyMetadata;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * GenericDevicePropertiesImpl Tester.
 *
 */
public class GenericDevicePropertiesImplTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private GenericDevicePropertyMetadata metadataFloat;
    private GenericDevicePropertyMetadata[] metadataArr;
    private GenericDevicePropertiesImpl props;

    @Before
    public void setUp() throws Exception {
        metadataFloat = context.mock(GenericDevicePropertyMetadata.class, "metadataFloat");
        metadataArr = new GenericDevicePropertyMetadata[]{metadataFloat};

        context.checking(new Expectations() {{
            allowing(metadataFloat).getDefaultNumberValue();
            will(returnValue(42.0f));
            allowing(metadataFloat).getDefaultStringValue();
            will(returnValue("42.0"));
            allowing(metadataFloat).getName();
            will(returnValue("fProp"));
            allowing(metadataFloat).getType();
            will(returnValue(Float.class));
            allowing(metadataFloat).getMinValue();
            will(returnValue(Float.NEGATIVE_INFINITY));
            allowing(metadataFloat).getMaxValue();
            will(returnValue(Float.POSITIVE_INFINITY));
            allowing(metadataFloat).serialize(GenericDevice.FORMAT_JSON);
            will(returnValue("{\"type\":\"float\"}"));
        }});

        props = new GenericDevicePropertiesImpl(metadataArr, null);
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSerialize() throws GenericDeviceException {
        context.checking(new Expectations() {{
        }});
        String json = props.serialize(GenericDevice.FORMAT_JSON);
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

    @Test
    public void testSerializeState() {
        String json = props.serializeState();
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
