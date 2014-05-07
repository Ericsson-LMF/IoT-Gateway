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
