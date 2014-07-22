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
package com.ericsson.deviceaccess.tutorial;

import com.ericsson.commonutil.LegacyUtil;
import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.service.homeautomation.hvac.TemperatureSensor;
import com.ericsson.deviceaccess.api.service.homeautomation.hvac.thermostat.ThermostatMode;
import com.ericsson.deviceaccess.api.service.homeautomation.hvac.thermostat.ThermostatSetPoint;
import com.ericsson.deviceaccess.api.service.util.PowerControl;
import com.ericsson.deviceaccess.api.service.util.PowerMeter;
import com.ericsson.deviceaccess.spi.schema.based.SBGenericDevice;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDevice;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDeviceUpdateListener;
import java.util.HashMap;
import java.util.Map;
import org.osgi.framework.ServiceRegistration;

/**
 * Implementation of a GenericDevice to realize a pseudo device run in the OSGi
 * framework. It implements several pseudo services with actions to print some
 * messages upon requests. This class is instantiated when a PseudoDevice is
 * discovered by the tutorial basedriver and registered with the DeviceFactory.
 */
public class GenericDevicePseudoImpl extends SBGenericDevice implements
        PseudoDeviceUpdateListener {

    private ServiceRegistration devReg;
    PseudoDevice pdev;

    public GenericDevicePseudoImpl(PseudoDevice device) {
        this.pdev = device;
        init();
    }

    void init() {
        pdev.setParameterUpdateListener(this);

        setURN(pdev.getURN());
        setId(pdev.getId());
        setOnline(true);
        setName("Device for tutorial");
        setType("power switch");
        setProtocol("pseudo");

        HashMap services = new HashMap();
        services.put(PowerControl.SERVICE_NAME, new PowerControlImpl(pdev));
        services.put(PowerMeter.SERVICE_NAME, new PowerMeterImpl(pdev));
        services.put(CustomService.SERVICE_NAME, new CustomService());
        services.put(AugmentedDimming.SERVICE_NAME, new AugmentedDimming());
        services.put(TemperatureSensor.SERVICE_NAME, new TempSensorImpl());
        services.put(ThermostatSetPoint.SERVICE_NAME,
                new ThermostatSetpointImpl());
        services.put(ThermostatMode.SERVICE_NAME, new ThermostatModeImpl());
        setService(services);
    }

    void destroy() {
        pdev.unsetParameterUpdateListener(this);
    }

    void setServiceRegistration(ServiceRegistration reg) {
        this.devReg = reg;
    }

    ServiceRegistration getServiceRegistration() {
        return devReg;
    }

    private void notifyUpdate(String path) {
        if (devReg != null) {
            Map<String, Object> props = this.getDeviceProperties();
            props.put(Constants.UPDATED_PATH, path);
            devReg.setProperties(LegacyUtil.toDictionary(props));
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.ericsson.deviceaccess.tutorial.pseudo.PseudoDeviceUpdateListener#
     * pseudoDeviceUpdated(java.lang.String)
     */
    @Override
    public void pseudoDeviceUpdated(String data, boolean active) {
        PowerMeterImpl powerMeterService = (PowerMeterImpl) getService(PowerMeter.SERVICE_NAME);
        powerMeterService.setCurrentPower(Float.parseFloat(data));
        notifyUpdate(powerMeterService.getPath(true));
        CustomService customService = (CustomService) getService(CustomService.SERVICE_NAME);
        customService.update(active);
        TempSensorImpl temperatureSensor = (TempSensorImpl) getService(TemperatureSensor.SERVICE_NAME);
        ThermostatSetpointImpl thermostatSetpoint = (ThermostatSetpointImpl) getService(ThermostatSetPoint.SERVICE_NAME);
        temperatureSensor.updateTemp(thermostatSetpoint
                .getCurrentDesiredHeatingTemperature());
        notifyUpdate(customService.getPath(true));
    }
}
