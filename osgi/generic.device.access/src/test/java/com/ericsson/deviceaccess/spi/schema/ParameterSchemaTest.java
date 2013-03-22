package com.ericsson.deviceaccess.spi.schema;

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import junit.framework.Assert;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * ParameterSchema Tester.
 */
public class ParameterSchemaTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private ParameterSchema intParameterSchema;
    private ParameterSchema floatParameterSchema;
    private ParameterSchema stringParameterSchema;

    @Before
    public void setUp() throws Exception {
        intParameterSchema = new ParameterSchema.Builder("intPar").
                setType(Integer.class).
                setDefaultValue(10).
                setMinValue("-10").
                setMaxValue("10").
                build();
        floatParameterSchema = new ParameterSchema.Builder("floatPar").
                setType(Float.class).
                setDefaultValue(42.0f).
                setMinValue("-10.0").
                setMaxValue("10.0").
                build();
        stringParameterSchema = new ParameterSchema.Builder("stringPar").
                setType(String.class).
                setDefaultValue("apa").
                setValidValues(new String[]{"apa", "banan"}).
                build();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testSerializeInt() throws GenericDeviceException {
        String json = intParameterSchema.serialize(GenericDevice.FORMAT_JSON);
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
    public void testSerializeFloat() throws GenericDeviceException {
        String json = floatParameterSchema.serialize(GenericDevice.FORMAT_JSON);
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
    public void testSerializeString() throws GenericDeviceException {
        String json = stringParameterSchema.serialize(GenericDevice.FORMAT_JSON);
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
