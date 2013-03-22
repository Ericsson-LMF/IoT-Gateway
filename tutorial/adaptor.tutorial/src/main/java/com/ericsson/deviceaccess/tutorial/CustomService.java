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
import com.ericsson.deviceaccess.spi.schema.*;

/**
 * Example of a custom service which is not defined in the service.xml, and for
 * which there exists no base class.
 */
public class CustomService extends SchemaBasedServiceBase {
    public static final String SERVICE_NAME = "MyCustomService";

    /**
     * This is the schema for the action "MyCustomAction", which accepts the
     * following arguments:
     * <ul>
     * <li>arg1:String</li>
     * <li>arg2:Int with default value: 20
     * </ul>
     * and returns the following results
     * <ul>
     * <li>res1:Float with default value: 0.0</li>
     * <li>res2:String with valid values: "On" and "Off"</li>
     * </ul>
     */
    private static ActionSchema MY_ACTION = new ActionSchema.Builder("MyCustomAction").
            addArgumentSchema(new ParameterSchema.Builder("arg1").
                    setType(String.class).
                    build()).
            addArgumentSchema(new ParameterSchema.Builder("arg2").
                    setType(Integer.class).
                    setDefaultValue(new Integer(20)).
                    setMinValue("10").
                    setMaxValue("45").
                    build()).
            addResultSchema(new ParameterSchema.Builder("res1").
                    setType(Float.class).
                    setDefaultValue(new Float(0.0)).
                    build()).
            addResultSchema(new ParameterSchema.Builder("res2").
                    setType(String.class)
                    .setDefaultValue("On")
                    .setValidValues(new String[]{"On", "Off"}).
                            build()).
            build();

    /**
     * This is the schema for this custom service. Define a schema
     * "MyCustomService" with:
     * <ul>
     * <li>one action "MyCustomAction"</li>
     * <li>and one property (state): "prop1", which is a String with valid
     * values: "Active" and "Inactive"</li>
     * </ul>
     */
    private static ServiceSchema SERVICE_SCHEMA = new ServiceSchema.Builder(SERVICE_NAME).
            addActionSchema(MY_ACTION).
            addPropertySchema(new ParameterSchema.Builder("prop1").
                    setType(String.class).
                    setDefaultValue("Active").
                    setValidValues(new String[]{"Active", "Inactive"}).
                    build()).
            build();

    /**
     * Create an instance.
     */
    public CustomService() {
        super(SERVICE_SCHEMA);

        // Define the action 
        defineAction("MyCustomAction", new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String arg1 = context.getArguments().getStringValue("arg1");
                int arg2 = context.getArguments().getIntValue("arg2");
                context.getResult().getValue().setFloatValue("res1", arg2 + arg1.length());
                context.getResult().getValue().setStringValue("res2", arg2 + arg1.length() > 30 ? "On" : "Off");
            }
        });
    }

    /**
     * This method is called by the base driver which simulates updates when the active state of the custom device.
     * <p/>
     * It updates the <i>prop1</i> property using the <i>getProperties().setStringValue(...)</i>
     * method provided by the base class.
     *
     * @param active
     */
    public void update(boolean active) {
        getProperties().setStringValue("prop1", active ? "Active" : "Inactive");
    }
}
