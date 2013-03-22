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

import com.ericsson.deviceaccess.spi.service.homeautomation.hvac.TemperatureSensorBase;

/**
 * Adaptor specific implementation of the <i>TemperatureSensor</i> service.
 */
public class TempSensorImpl extends TemperatureSensorBase {
    {
        System.out.println("Created "+this);
    }
    protected void refreshProperties() {

    }

    public void updateTemp(float setThermostatTemp) {
        float temp = (float) (setThermostatTemp + (getCurrentTemperature() + ((Math.random() - 0.5f) - setThermostatTemp)) / 2.0f);
        updateCurrentTemperature(temp);
    }
}
