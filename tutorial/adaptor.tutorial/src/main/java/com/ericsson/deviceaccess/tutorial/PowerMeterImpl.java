/*
 * Copyright (c) Ericsson AB, 2011.
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
import com.ericsson.deviceaccess.spi.service.util.PowerMeterBase;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDevice;

/**
 * Adaptor specific implementation of the <i>PowerMeter</i> service.
 */
public class PowerMeterImpl extends PowerMeterBase {
    PseudoDevice dev;

    public PowerMeterImpl(PseudoDevice dev) {
        this.dev = dev;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This is the adaptor specific implementation of the <i>GetPower</i> action.
     * <p/>
     * It will be called by the base class when a client invokes the action.
     */
    public GetPowerResult executeGetPower() throws GenericDeviceException {
        GetPowerResult result = new GetPowerResult();
        try {
            result.Power = Float.parseFloat(dev.getConsumedPowerInWatt());
        } catch (Exception e) {
            throw new GenericDeviceException(500, "Exception", e);
        }
        return result;
    }

    /**
     * This method is called by the base driver which simulates updates when the current power
     * in the device changes.
     * <p/>
     * It updates the <i>CurrentPower</i> property using the <i>updateCurrentPower(...)</i>
     * method provided by the base class.
     *
     * @param currentPower
     */
    void setCurrentPower(float currentPower) {
        updateCurrentPower(currentPower);
    }

    protected void refreshProperties() {
        // NOP
    }
}
