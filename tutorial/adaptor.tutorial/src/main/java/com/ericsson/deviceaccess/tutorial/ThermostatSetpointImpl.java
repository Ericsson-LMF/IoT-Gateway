/*
 * Copyright (c) Ericsson AB, 2012.
 *
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 *
 * ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ERICSSON SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */
package com.ericsson.deviceaccess.tutorial;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.spi.service.homeautomation.hvac.thermostat.ThermostatSetPointBase;

/**
 * Adaptor specific implementation of the <i>ThermostatSetPoint</i> service.
 */
public class ThermostatSetpointImpl extends ThermostatSetPointBase {
    {
        System.out.println("Created "+this);
        updateCurrentDesiredCoolingTemperature(20.0f);
        updateCurrentDesiredHeatingTemperature(20.0f);
    }

    protected void refreshProperties() {
    }

    public void executeSetDesiredCoolingTemperature(float desiredTemperature) throws GenericDeviceException {
        updateCurrentDesiredCoolingTemperature(desiredTemperature);
        System.out.println("Desired cooling temperature has been set to: " + desiredTemperature);
    }

    public void executeSetDesiredHeatingTemperature(float desiredTemp) throws GenericDeviceException {
        updateCurrentDesiredHeatingTemperature(desiredTemp);
        System.out.println("Desired heating temperature has been set to: " + desiredTemp);
    }
}
