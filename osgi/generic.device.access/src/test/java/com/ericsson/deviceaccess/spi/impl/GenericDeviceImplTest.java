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

import com.ericsson.commonutil.serialization.Format;
import com.ericsson.deviceaccess.api.genericdevice.GDEventListener;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties.Data;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import com.ericsson.deviceaccess.spi.event.EventManager;
import com.ericsson.deviceaccess.spi.genericdevice.GDActivator;
import com.ericsson.deviceaccess.spi.genericdevice.GDService;
import com.ericsson.research.common.testutil.ReflectionTestUtil;
import java.util.HashMap;
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
    private GDService dummyService;
    private GDProperties dummyProperties;
    private GDPropertyMetadata dummyMeta;

    @Before
    public void setUp() throws Exception {
        dummyService = context.mock(GDService.class);
        eventManager = context.mock(EventManager.class);
        dummyProperties = context.mock(GDProperties.class);
        dummyMeta = context.mock(GDPropertyMetadata.class);
        ReflectionTestUtil.setField(GDActivator.class, "eventManager", eventManager);
        device = new GenericDeviceImpl() {
        };

        context.checking(new Expectations() {
            {
                allowing(dummyService).getName();
                will(returnValue("serv"));
                allowing(dummyService).updatePath(with(any(String.class)));
                allowing(dummyService).setParentDevice(device);
                allowing(dummyService).getActions();
                will(returnValue(new HashMap<>()));
                allowing(dummyService).getProperties();
                will(returnValue(dummyProperties));
                allowing(dummyService).getPath();
                will(returnValue(""));
                allowing(dummyProperties).getProperties();
                will(returnValue(new HashMap<String, Data>() {
                    {
                        put("prop", new Data(dummyMeta).set(10));
                    }
                }
                ));
                allowing(dummyMeta).getDefaultStringValue();
                will(returnValue(""));
                allowing(dummyMeta).getMaxValue();
                will(returnValue(null));
                allowing(dummyMeta).getMinValue();
                will(returnValue(null));
                allowing(dummyMeta).getTypeName();
                will(returnValue(null));
                allowing(dummyMeta).getValidValues();
                will(returnValue(new String[0]));
            }
        });

        device.setId("devId");
        device.setURN("devUrn");
        device.setName("dev");
        device.setProtocol("prot");
        device.putService(dummyService);

    }

    @After
    public void tearDown() throws Exception {
        ReflectionTestUtil.setField(GDActivator.class, "eventManager", null);
    }

    @Test
    public void testEvents() {

        context.checking(new Expectations() {
            {
                oneOf(eventManager).addPropertyEvent("devId", "DeviceProperties", new HashMap() {
                    {
                        put(GDEventListener.DEVICE_ONLINE, true);
                    }
                });

                oneOf(eventManager).addPropertyEvent("devId", "DeviceProperties", new HashMap() {
                    {
                        put(GDEventListener.DEVICE_NAME, "banan");
                    }
                });
            }
        });

        device.setOnline(true);
        device.setOnline(true);
        device.setName("banan");
        device.setName("banan");

        context.assertIsSatisfied();
    }

    @Test
    public void testSerialize() throws GDException, JSONException {
        context.checking(new Expectations() {
            {
            }
        });
        String json = device.serialize(Format.JSON);
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
    public void testSerializeState() throws GDException {
        context.checking(new Expectations() {
            {
            }
        });
        String json = device.serializeState();
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
