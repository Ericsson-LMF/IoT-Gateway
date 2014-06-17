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
package com.ericsson.deviceaccess.adaptor.ruleengine.device;

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.genericdevice.GDEventListener;
import com.ericsson.deviceaccess.api.genericdevice.GDService;
import com.ericsson.research.commonutil.LegacyUtil;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public enum PropertyManager implements GDEventListener, ServiceTrackerCustomizer {

    /**
     * Singleton.
     */
    INSTANCE;

    ServiceTracker deviceTracker;
    Map<String, GenericDevice> devices = new HashMap<>();
    Map<String, Object> deviceProperties = new HashMap<>();
    private BundleContext context;
    private ServiceRegistration sr;
    private RuleService ruleService;
    final Object mutex = new Object();

    public void start(BundleContext context, RuleService rs) {
        this.context = context;
        this.ruleService = rs;

        deviceTracker = new ServiceTracker(context, GenericDevice.class.getName(), this);
        deviceTracker.open();

        Map<String, Object> props = new HashMap<>();
        props.put(GDEventListener.GENERICDEVICE_FILTER, "(device.id=*)");
        sr = context.registerService(GDEventListener.class, this, LegacyUtil.toDictionary(props));
    }

    public void stop() {
        ruleService = null;
        deviceTracker.close();
        sr.unregister();
    }

    public Map<String, Object> getDeviceProperties() {
        return deviceProperties;
    }

    @Override
    public void notifyGenericDeviceEvent(String deviceId, String serviceName, Map<String, Object> properties) {
        if (properties == null) {
            return;
        }

        // Ignore updates of this property for performance reasons
        if (properties.get("lastUpdateTime") != null) {
            return;
        }

        synchronized (mutex) {
            if ("DeviceProperties".equals(serviceName) && properties.get(GDEventListener.DEVICE_STATE) != null && properties.get(GDEventListener.DEVICE_STATE).equals("Ready")) {
                GenericDevice device = devices.get(deviceId);
                if (device != null) {
                    updateDevice(device);
                }
            } else {
                properties.forEach((key, value) -> {
                    deviceProperties.put(deviceId + "." + serviceName + "." + key, value);
                    System.out.println(deviceId + "." + serviceName + "." + key + "=" + value);
                });
            }
        }

        properties.forEach((key, value) -> {
            //TODO: Update weekDay and timeOfDay
            ruleService.handlePropertyUpdate(deviceProperties, deviceId, serviceName, key);
        });
    }

    @Override
    public void notifyGenericDevicePropertyAddedEvent(String deviceId, String serviceName, String propertyId) {
        // TOOD: Add property?
    }

    @Override
    public void notifyGenericDevicePropertyRemovedEvent(String deviceId, String serviceName, String propertyId) {
        synchronized (mutex) {
            deviceProperties.remove(deviceId + "." + serviceName + "." + propertyId);
        }
    }

    @Override
    public Object addingService(ServiceReference reference) {
        GenericDevice device = (GenericDevice) context.getService(reference);
        devices.put(device.getId(), device);
        updateDevice(device);
        return null;
    }

    @Override
    public void modifiedService(ServiceReference reference, Object service) {
    }

    @Override
    public void removedService(ServiceReference reference, Object service) {
        GenericDevice device = (GenericDevice) context.getService(reference);

        synchronized (mutex) {
            // Remove all service properties
            String[] serviceNames = device.getServiceNames();
            for (String serviceName : serviceNames) {
                GDService srv = device.getService(serviceName);
                String[] propertyIds = srv.getProperties().getNames();
                for (String propertyId : propertyIds) {
                    Object value = srv.getProperties().getValue(propertyId);
                    deviceProperties.remove(device.getId() + "." + serviceName + "." + propertyId);
                }
            }

            // Remove all device properties
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.id");
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.manufacturer");
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.modelName");
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.name");
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.protocol");
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.state");
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.type");
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.location");
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.urn");
            deviceProperties.remove(device.getId() + ".DeviceProperties.device.online");
        }
    }

    private void updateDevice(GenericDevice device) {
        synchronized (mutex) {
            // Add or update all service properties
            String[] serviceNames = device.getServiceNames();
            for (String serviceName : serviceNames) {
                GDService service = device.getService(serviceName);
                String[] propertyIds = service.getProperties().getNames();
                for (String propertyId : propertyIds) {
                    if ("lastUpdateTime".equals(propertyId)) {
                        continue;
                    }
                    Object value = service.getProperties().getValue(propertyId);
                    if (value != null) {
                        System.out.println(device.getId() + "." + serviceName + "." + propertyId + "=" + value);
                        deviceProperties.put(device.getId() + "." + serviceName + "." + propertyId, value);
                    }
                }
            }

            // Add or update all device properties
            deviceProperties.put(device.getId() + ".DeviceProperties.device.id", device.getId());
            deviceProperties.put(device.getId() + ".DeviceProperties.device.manufacturer", device.getManufacturer());
            deviceProperties.put(device.getId() + ".DeviceProperties.device.modelName", device.getModelName());
            deviceProperties.put(device.getId() + ".DeviceProperties.device.name", device.getName());
            deviceProperties.put(device.getId() + ".DeviceProperties.device.protocol", device.getProtocol());
            deviceProperties.put(device.getId() + ".DeviceProperties.device.state", device.getState());
            deviceProperties.put(device.getId() + ".DeviceProperties.device.type", device.getType());
            deviceProperties.put(device.getId() + ".DeviceProperties.device.location", device.getLocation());
            deviceProperties.put(device.getId() + ".DeviceProperties.device.urn", device.getURN());
            deviceProperties.put(device.getId() + ".DeviceProperties.device.online", device.isOnline());
        }
    }
}
