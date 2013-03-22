package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceEventListener;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.spi.GenericDeviceActivator;
import com.ericsson.deviceaccess.spi.GenericDeviceService;
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
 * GenericDeviceImpl Tester.
 *
 */
public class GenericDeviceImplTest {
    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private EventManager eventManager;
    private GenericDeviceImpl device;
    private GenericDeviceService dummyService;

    @Before
    public void setUp() throws Exception {
        dummyService = context.mock(GenericDeviceService.class);
        eventManager = context.mock(EventManager.class);
        ReflectionTestUtil.setField(GenericDeviceActivator.class, "eventManager", eventManager);
        device = new GenericDeviceImpl() {
        };

        context.checking(new Expectations(){{
            allowing(dummyService).getName();will(returnValue("serv"));
            allowing(dummyService).updatePath(with(any(String.class)));
            allowing(dummyService).setParentDevice(device);
        }});

        device.setId("devId");
        device.setURN("devUrn");
        device.setName("dev");
        device.setProtocol("prot");
        device.putService(dummyService);

    }

    @After
    public void tearDown() throws Exception {
        ReflectionTestUtil.setField(GenericDeviceActivator.class, "eventManager", null);
    }

    @Test
    public void testEvents() {

        context.checking(new Expectations(){{
            oneOf(eventManager).notifyGenericDeviceEvent("devId", "DeviceProperties", new Properties(){{
                put(GenericDeviceEventListener.DEVICE_ONLINE, true);
            }});

            oneOf(eventManager).notifyGenericDeviceEvent("devId", "DeviceProperties", new Properties(){{
                put(GenericDeviceEventListener.DEVICE_NAME, "banan");
            }});
        }});

        device.setOnline(true);
        device.setOnline(true);
        device.setName("banan");
        device.setName("banan");

        context.assertIsSatisfied();
    }

    @Test
    public void testSerialize() throws GenericDeviceException, JSONException {
        context.checking(new Expectations(){{
            oneOf(dummyService).serialize(GenericDevice.FORMAT_JSON);will(returnValue("{\"name\":\"serv\"}"));
        }});

        String json = device.serialize(GenericDevice.FORMAT_JSON);

        context.assertIsSatisfied();
        // Just check that JSON parsing works
        try {
            JSONObject jsonObject = new JSONObject(json);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSerializeState() throws GenericDeviceException {
        context.checking(new Expectations(){{
            oneOf(dummyService).serializeState();will(returnValue("{\"name\":\"serv\"}"));
        }});

        String json = device.serializeState();
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
