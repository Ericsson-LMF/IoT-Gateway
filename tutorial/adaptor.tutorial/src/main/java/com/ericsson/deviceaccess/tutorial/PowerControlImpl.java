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
import com.ericsson.deviceaccess.spi.service.util.PowerControlBase;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDevice;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDeviceException;

/**
 * Adaptor specific implementation of the <i>PowerControl</i> service.
 */
public class PowerControlImpl extends PowerControlBase {
    PseudoDevice dev;

    public PowerControlImpl(PseudoDevice dev) {
        this.dev = dev;
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This is the adaptor specific implementation of the <i>On</i> action.
     * <p/>
     * It will be called by the base class when a client invokes the action.
     */
    public void executeOn() throws GenericDeviceException {
        try {
            dev.powerOn();
            updateCurrentState(VALUE_PROP_CurrentState_On);
        } catch (PseudoDeviceException e) {
            throw new GenericDeviceException(500, "Failed to execute: " + e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This is the adaptor specific implementation of the <i>Off</i> action.
     * <p/>
     * It will be called by the base class when a client invokes the action.
     */
    public void executeOff() throws GenericDeviceException {
        try {
            dev.powerOff();
            updateCurrentState(VALUE_PROP_CurrentState_Off);
        } catch (PseudoDeviceException e) {
            throw new GenericDeviceException(500, "Failed to execute: " + e.getMessage());
        }
    }

    protected void refreshProperties() {
        // NOP
    }
}
