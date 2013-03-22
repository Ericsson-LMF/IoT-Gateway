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

import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.spi.schema.ActionDefinition;
import com.ericsson.deviceaccess.spi.schema.ActionSchema;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.service.homeautomation.lighting.DimmingBase;

/**
 * Adaptor specific implementation of the <i>Dimming</i> service, which has been augmented with the
 * "MyAugmentAction" action.
 */
public class AugmentedDimming extends DimmingBase {
    private static ActionSchema MY_ACTION = new ActionSchema.Builder("MyAugmentAction").
            addArgumentSchema(new ParameterSchema.Builder("arg1").
                    setType(String.class).
                    build()).
            addResultSchema(new ParameterSchema.Builder("res1").
                    setType(Integer.class).
                    setDefaultValue(new Integer(0)).
                    build()).
            build();
    private int currentLoadLevel;


    /**
     * Create the instance.
     */
    public AugmentedDimming() {
        // Define the custom action which augments this service
        defineCustomAction(MY_ACTION, new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String arg1 = context.getArguments().getStringValue("arg1");
                context.getResult().getValue().setIntValue("res1", arg1.length());
            }
        });
    }

    protected void refreshProperties() {
        updateCurrentLoadLevel(Math.min((int) (currentLoadLevel + ((Math.random() * 10) - 5)), 100));
    }

    /**
     * {@inheritDoc}
     * <p/>
     * This is the adaptor specific implementation of the <i>SetLoadLevelTarget</i> action.
     * <p/>
     * It will be called by the base class when a client invokes the action.
     */
    public void executeSetLoadLevelTarget(int loadLevelTarget) throws GenericDeviceException {
        currentLoadLevel = loadLevelTarget;
        System.out.println("Set load level target: " + loadLevelTarget);
    }

    public void executeSetLoadLevelTargetWithRate(int loadLevelTarget, float rate) throws GenericDeviceException {
        currentLoadLevel = loadLevelTarget;
        System.out.println("Set load level target: " + loadLevelTarget +" with rate: "+rate);
    }

    public void executeOn() throws GenericDeviceException {
        System.out.println("Execute ON");
    }

    public void executeOff() throws GenericDeviceException {
        System.out.println("Execute OFF");
    }
}
