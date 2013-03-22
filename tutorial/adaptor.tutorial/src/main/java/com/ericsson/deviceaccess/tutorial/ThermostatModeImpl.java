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
import com.ericsson.deviceaccess.api.service.homeautomation.hvac.thermostat.ThermostatMode;
import com.ericsson.deviceaccess.spi.service.homeautomation.hvac.thermostat.ThermostatModeBase;

/**
 * Adaptor specific implementation of the <i>ThermostatMode</i> service.
 */
public class ThermostatModeImpl extends ThermostatModeBase {
    {
        System.out.println("Created " + this);
        updateMode(ThermostatMode.VALUE_PROP_mode_Heat);
    }

    protected void refreshProperties() {
    }

    public void executeSetMode(String s) throws GenericDeviceException {
        updateMode(s);
        System.out.println("Thermostat mode has been set to: " +s);
    }
}
