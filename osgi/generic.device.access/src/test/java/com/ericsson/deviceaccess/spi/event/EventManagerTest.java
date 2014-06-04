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
package com.ericsson.deviceaccess.spi.event;

import com.ericsson.deviceaccess.api.GenericDeviceEventListener;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceImpl;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Action;
import org.jmock.api.Invocation;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.Filter;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * EventManager Tester.
 */
public class EventManagerTest {

    static ShutdownEventManager shutdown(EventManager eventManager) {
        return new ShutdownEventManager(eventManager);
    }

    private Mockery context = new Mockery() {
        {
            setImposteriser(ClassImposteriser.INSTANCE);
        }
    };
    private EventManager eventManager;
    private BundleContext bundleContext;
    private GenericDeviceEventListener listener;
    private ServiceReference serviceReference;
    private Timer timer;

    private void registerDevice(final String id) {
        final GenericDeviceImpl device = new GenericDeviceImpl() {
        };
        device.setId(id);
        final ServiceReference deviceReference = context.mock(ServiceReference.class, "deviceReference" + id);
        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getService(deviceReference);
                will(returnValue(device));
                allowing(listener).notifyGenericDeviceEvent(with(aNonNull(String.class)), with(any(String.class)), with(new Hashtable<String, Object>() {
                    {
                        put("device.state", "Ready");
                    }
                }));
            }
        });
        eventManager.addingService(deviceReference);
    }

    @Before
    public void setUp() throws Exception {
        serviceReference = context.mock(ServiceReference.class, "serviceReference");
        bundleContext = context.mock(BundleContext.class);
        listener = context.mock(GenericDeviceEventListener.class);
        eventManager = new EventManager();
        eventManager.setContext(bundleContext);
        timer = new Timer();
        timer.schedule(new ShutdownTask(), 3000);

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).createFilter(with(aNonNull(String.class)));
                allowing(bundleContext).addServiceListener(with(any(ServiceListener.class)), with(aNonNull(String.class)));
                allowing(bundleContext).removeServiceListener(with(any(ServiceListener.class)));
                allowing(bundleContext).getServiceReferences(with("com.ericsson.deviceaccess.api.GenericDevice"), with(aNull(String.class)));
            }
        });

        registerDevice("zwave32");
        registerDevice("zwave31");
        registerDevice("dev");
    }

    @After
    public void tearDown() throws Exception {
        if (timer != null) {
            timer.cancel();
        }
        eventManager.shutdown();
    }

    @Test
    public void testNullFilter() throws InvalidSyntaxException {
        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = null;

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));

                oneOf(listener).notifyGenericDeviceEvent(with("dev"), with("srv"), with(new Hashtable<String, Object>() {
                    {
                        put("device.online", true);
                    }
                }));
                will(shutdown(eventManager));
            }
        });

        eventManager.addEvent("dev", "srv", new Hashtable<String, Object>() {
            {
                put(GenericDeviceEventListener.DEVICE_ONLINE, true);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();
    }

    @Test
    public void test_Filter_specific_device() throws InvalidSyntaxException {
        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = "(&(device.id=zwave31)(|(temp >= 30)(power <= 100)))";

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));

                oneOf(listener).notifyGenericDeviceEvent(with("zwave31"), with("srv"), with(new Hashtable<String, Object>() {
                    {
                        put("power", 100);
                    }
                }));
                oneOf(listener).notifyGenericDeviceEvent(with("zwave31"), with("srv"), with(new Hashtable<String, Object>() {
                    {
                        put("temp", 30);
                    }
                }));
                will(shutdown(eventManager));
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("power", 100);
            }
        });
        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 30);
            }
        });
        eventManager.addEvent("zwave32", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 30);
            }
        });
        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 101);
            }
        });
        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 29);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();
    }

    @Test
    public void test_Filter_all_devices_and_services() throws InvalidSyntaxException {
        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = "(|(temp >= 30)(power <= 100))";

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));

                oneOf(listener).notifyGenericDeviceEvent(with("zwave31"), with("srv1"), with(new Hashtable<String, Object>() {
                    {
                        put("power", 100);
                    }
                }));
                oneOf(listener).notifyGenericDeviceEvent(with("zwave32"), with("srv2"), with(new Hashtable<String, Object>() {
                    {
                        put("temp", 30);
                    }
                }));
                will(shutdown(eventManager));
            }
        });

        eventManager.addEvent("zwave31", "srv1", new Hashtable<String, Object>() {
            {
                put("power", 100);
            }
        });
        eventManager.addEvent("zwave32", "srv2", new Hashtable<String, Object>() {
            {
                put("temp", 30);
            }
        });
        eventManager.addEvent("zwave31", "srv1", new Hashtable<String, Object>() {
            {
                put("temp", 101);
            }
        });
        eventManager.addEvent("zwave32", "srv2", new Hashtable<String, Object>() {
            {
                put("temp", 29);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();
    }

    @Test
    public void test_Filter_specific_service() throws InvalidSyntaxException {
        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = "(&(service.name=srv)(|(temp >= 30)(power <= 100)))";

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));

                oneOf(listener).notifyGenericDeviceEvent(with("zwave31"), with("srv"), with(new Hashtable<String, Object>() {
                    {
                        put("power", 100);
                    }
                }));
                oneOf(listener).notifyGenericDeviceEvent(with("zwave32"), with("srv"), with(new Hashtable<String, Object>() {
                    {
                        put("temp", 30);
                    }
                }));
                will(shutdown(eventManager));
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("power", 100);
            }
        });
        eventManager.addEvent("zwave32", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 30);
            }
        });
        eventManager.addEvent("zwave33", "srv5", new Hashtable<String, Object>() {
            {
                put("temp", 30);
            }
        });
        eventManager.addEvent("zwave34", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 101);
            }
        });
        eventManager.addEvent("zwave35", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 29);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();
    }

    @Test
    public void test_Filter_specific_device_and_service() throws InvalidSyntaxException {
        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = "(&(device.id=zwave31)(service.name=srv)(|(temp >= 30)(power <= 100)))";

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));

                oneOf(listener).notifyGenericDeviceEvent(with("zwave31"), with("srv"), with(new Hashtable<String, Object>() {
                    {
                        put("power", 100);
                    }
                }));
                will(shutdown(eventManager));
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("power", 100);
            }
        });

        eventManager.addEvent("zwave31", "srv2", new Hashtable<String, Object>() {
            {
                put("temp", 30);
            }
        });
        eventManager.addEvent("zwave33", "srv5", new Hashtable<String, Object>() {
            {
                put("temp", 30);
            }
        });
        eventManager.addEvent("zwave34", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 101);
            }
        });
        eventManager.addEvent("zwave35", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 29);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();
    }

    @Test
    public void test_Filter_online_device() throws InvalidSyntaxException {
        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = "(&(device.online=true)(temp =*))";

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));

                oneOf(listener).notifyGenericDeviceEvent(with("zwave32"), with("srv2"), with(new Hashtable<String, Object>() {
                    {
                        put(GenericDeviceEventListener.DEVICE_ONLINE, true);
                        put("temp", 30);
                    }
                }));
                will(shutdown(eventManager));
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put(GenericDeviceEventListener.DEVICE_ONLINE, true);
                put("power", 100);
            }
        });
        eventManager.addEvent("zwave32", "srv2", new Hashtable<String, Object>() {
            {
                put(GenericDeviceEventListener.DEVICE_ONLINE, true);
                put("temp", 30);
            }
        });
        eventManager.addEvent("zwave33", "srv5", new Hashtable<String, Object>() {
            {
                put(GenericDeviceEventListener.DEVICE_ONLINE, false);
                put("temp", 30);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();
    }

    @Test
    public void test_Filter_protocol_device() throws InvalidSyntaxException {
        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = "(&(device.protocol=banan)(temp =*))";

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));

                oneOf(listener).notifyGenericDeviceEvent(with("zwave32"), with("srv2"), with(new Hashtable<String, Object>() {
                    {
                        put(GenericDeviceEventListener.DEVICE_PROTOCOL, "banan");
                        put("temp", 30);
                    }
                }));
                will(shutdown(eventManager));
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put(GenericDeviceEventListener.DEVICE_PROTOCOL, "banan");
                put("power", 100);
            }
        });
        eventManager.addEvent("zwave32", "srv2", new Hashtable<String, Object>() {
            {
                put(GenericDeviceEventListener.DEVICE_PROTOCOL, "banan");
                put("temp", 30);
            }
        });
        eventManager.addEvent("zwave33", "srv5", new Hashtable<String, Object>() {
            {
                put(GenericDeviceEventListener.DEVICE_PROTOCOL, "apa");
                put("temp", 30);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();
    }

    @Test
    public void test_Filter_NoMatch() throws InvalidSyntaxException {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new ShutdownTask(), 1000);

        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = "(&(device.id=zwave31)(|(temp >= 30)(power <= 100)))";

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("temp", 29);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();
    }

    //Test that the delta filtering works using various datatypes
    @Test
    public void test_Filter_property_delta_float() throws InvalidSyntaxException {
        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = "(&(device.id=zwave31)(service.name=srv)(power__delta>=2))";

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));

                oneOf(listener).notifyGenericDeviceEvent(with("zwave31"), with("srv"), with(new Hashtable<String, Object>() {
                    {
                        put("power", (float) 104.34);
                        put("power__delta", (float) 4.24);
                    }
                }));

                will(shutdown(eventManager));
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("power", (float) 100.10);
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("power", (float) 104.34);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();

    }

    @Test
    public void test_Filter_property_delta_int() throws InvalidSyntaxException {
        final String eventManagerRegfilter = "(" + Constants.OBJECTCLASS + "=" + GenericDeviceEventListener.class.getName() + ")";
        final String listenerFilter = "(&(device.id=zwave31)(service.name=srv)(power__delta>=2))";

        context.checking(new Expectations() {
            {
                oneOf(bundleContext).getServiceReferences((String) null, eventManagerRegfilter);
                will(returnValue(new ServiceReference[]{serviceReference}));
                oneOf(bundleContext).getService(serviceReference);
                will(returnValue(listener));
                oneOf(serviceReference).getProperty(GenericDeviceEventListener.GENERICDEVICE_FILTER);
                will(returnValue(listenerFilter));

                oneOf(listener).notifyGenericDeviceEvent(with("zwave31"), with("srv"), with(new Hashtable<String, Object>() {
                    {
                        put("power", 100);
                        put("power__delta", 4);
                    }
                }));

                will(shutdown(eventManager));
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("power", 96);
            }
        });

        eventManager.addEvent("zwave31", "srv", new Hashtable<String, Object>() {
            {
                put("power", 100);
            }
        });

        eventManager.run();

        context.assertIsSatisfied();

    }

    @Test
    public void testFiltering() throws InvalidSyntaxException {
        Filter filter = FrameworkUtil.createFilter("(&(device.id=zwave31)(|(temp >= 30)(power <= 100)))");
        assertTrue(filter.match(new Hashtable<String, Object>() {
            {
                put("device.id", "zwave31");
                put("service.id", "banan");
                put("temp", 31);
            }
        }));

        assertTrue(filter.match(new Hashtable<String, Object>() {
            {
                put("device.id", "zwave31");
                put("service.id", "apple");
                put("power", 99);
            }
        }));

        assertFalse(filter.match(new Hashtable<String, Object>() {
            {
                put("device.id", "zwave31");
                put("service.id", "apple");
                put("temp", 29);
                put("power", 101);
            }
        }));

        filter = FrameworkUtil.createFilter("(&(device.id=32))");
        assertTrue(filter.match(new Hashtable<String, Object>() {
            {
                put("device.id", 32);
                put("service.id", "banan");
                put("p2", "NONE");
            }
        }));

        filter = FrameworkUtil.createFilter("(&(device.id=32)(p1 >= 20))");
        assertTrue(filter.match(new Hashtable<String, Object>() {
            {
                put("device.id", 32);
                put("p1", 22);
                put("p2", "NONE");
            }
        }));

        filter = FrameworkUtil.createFilter("(&(device.online=true)(|(CurrentPower=*)(CurrentTemperature=*)))");
        assertTrue(filter.match(new Hashtable<String, Object>() {
            {
                put("device.online", true);
                put("CurrentPower", 22);
            }
        }));
        assertFalse(filter.match(new Hashtable<String, Object>() {
            {
                put("device.online", false);
                put("CurrentPower", 22);
            }
        }));
    }

    static class ShutdownEventManager implements Action {

        private EventManager eventManager;

        ShutdownEventManager(EventManager eventManager) {
            this.eventManager = eventManager;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Shutdown the event manager");
        }

        @Override
        public Object invoke(Invocation invocation) throws Throwable {
            eventManager.shutdown();
            return null;
        }
    }

    private class ShutdownTask extends TimerTask {

        @Override
        public void run() {
            System.out.println("Shutdown");
            eventManager.shutdown();
        }
    }
}
