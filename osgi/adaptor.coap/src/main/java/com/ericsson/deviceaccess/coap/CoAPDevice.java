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
package com.ericsson.deviceaccess.coap;

import com.ericsson.common.util.LegacyUtil;
import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.genericdevice.GDService;
import com.ericsson.deviceaccess.spi.schema.based.SBGenericDevice;
import java.net.URI;
import java.util.Map;
import org.osgi.framework.ServiceRegistration;

/**
 * CoAPDevice
 *
 * This class represents a CoAP device inside of the adaptor. This is the class
 * that is registered to the OSGi framework thus enabling the access of the
 * device from the GDA, because it extends the GenericDeviceImpl class. This
 * class is also the place where the service schemas are attached (via the
 * putService() method call in the CoAPDeviceAgent class) and the the service
 * parameter updates are handled. Service parameter updates are when new data is
 * received from the device and passed on to the GDA.
 */
public class CoAPDevice extends SBGenericDevice {

    private ServiceRegistration serviceRegistration;

    /**
     * Set up the details of the CoAP Device
     *
     * @param uri
     */
    public CoAPDevice(URI uri) {
        this.setId(uri.getHost());

        setOnline(true);
        // setName("CurrentCost " + currentCostMonitor.getModelName());
        // setManufacturer(MANUFACTURER);
        // setModelName(currentCostMonitor.getModelName());
        // setProductClass(currentCostMonitor.getModelName());
        // setType("powerMeter");
        // setProtocol(CURRENT_COST);
        //System.out.println("[CoAPDevice]: getServiceNames: " + java.util.Arrays.toString(getServiceNames()));
    }

    /**
     * An service parameter update has been processed from a resource on a
     * device, pass it on to the GDA
     *
     * @param path
     */
    private void notifyUpdate(String path) {
        System.out.println("[CoAPDevice]: " + "in notifyUpdate()");
        if (serviceRegistration != null) {
            System.out.println("[CoAPDevice]: " + "about to set property");
            Map<String, Object> props = this.getDeviceProperties();
            props.put(Constants.UPDATED_PATH, path);
            serviceRegistration.setProperties(LegacyUtil.toDictionary(props));
        }

    }

    ServiceRegistration getServiceRegistration() {
        return serviceRegistration;
    }

    void setServiceRegistration(ServiceRegistration serviceRegistration) {
        this.serviceRegistration = serviceRegistration;
    }

    /**
     * An update has been received from a resource on a device, extract the
     * service name and key to enable the service parameter update
     *
     * @param data
     */
    public void deviceUpdate(String data) {
        System.out.println("[CoAPDevice]: in deviceUpdate()");
        // this code assumes that the stuff we want is in the first element of the array
        //System.out.println("[CoAPDevice]: " + " getServiceNames() " + java.util.Arrays.toString(getServiceNames()));
        GDService service = getServices().values().iterator().next();

        //System.out.println("[CoAPDevice]: " + " keys() " + java.util.Arrays.toString(service.getProperties().getNames()));
        String key = service.getProperties().getProperties().keySet().iterator().next();
        service.getProperties().setStringValue(key, data);
        // trigger update
        notifyUpdate(service.getPath(true));

    }
}
