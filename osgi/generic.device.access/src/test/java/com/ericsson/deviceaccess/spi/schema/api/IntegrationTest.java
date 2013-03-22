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

package com.ericsson.deviceaccess.spi.schema.api;

import com.ericsson.deviceaccess.api.GenericDeviceAction;
import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.spi.schema.*;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * 
 */
public class IntegrationTest {
    private static final String RESULT_NAME = "result";
    private static final String ARGUMENT_NAME = "argument1";
    private static final String PARAMETER_NAME = "parameter1";
    private static final String ACTION_NAME = "action1";
    private static final String SERVICE_NAME = "MyService";
    private static ServiceSchema serviceSchema;

    @BeforeClass
    public static void setup() {
        serviceSchema = new ServiceSchema.Builder(SERVICE_NAME).
                addActionSchema(new ActionSchema.Builder(ACTION_NAME).
                    setMandatory(true).
                    addArgumentSchema(new ParameterSchema.Builder(ARGUMENT_NAME).
                        setType(Integer.class).
                        setDefaultValue(0)
                        .setMinValue("-100").
                        setMaxValue("100").
                        build()).
                    addResultSchema(new ParameterSchema.Builder(RESULT_NAME).
                        setType(String.class).
                        setDefaultValue("banan").
                        setValidValues(new String[]{"result=42", "banan"}).
                        build()).
                    build()).
                addPropertySchema(new ParameterSchema.Builder(PARAMETER_NAME).
                    setType(Integer.class).
                    setDefaultValue(0).
                    build()).
                build();
    }

    @Test
    public void testCreateDevice_sucessfulInlineCustomDefinition() throws GenericDeviceException {
        final ServiceSchema serviceSchema = new ServiceSchema.Builder("CustomService").
                build();
        final ActionSchema actionSchema = new ActionSchema.Builder("CustomAction").
                setMandatory(true).
                addArgumentSchema(new ParameterSchema.Builder("CustomArgument").
                    setType(Integer.class).
                    setDefaultValue(0).
                    build()).
                addResultSchema(new ParameterSchema.Builder("CustomResult").
                    setType(String.class).
                    setDefaultValue("apa").
                    setValidValues(new String[]{"apa", "result=47"}).
                    build()).
                build();
        SchemaBasedGenericDevice myGenericDevice = new SchemaBasedGenericDevice() {
            {
                addSchemaBasedService(createService(serviceSchema).
                    defineCustomAction(actionSchema, new ActionDefinition() {
                        public void invoke(GenericDeviceActionContext context) {
                            int input = context.getArguments().getIntValue("CustomArgument");
                            String result = "result="+input;
                            context.getResult().getValue().setStringValue("CustomResult", result);
                        }
                    }));
            }
        };

        // Call action via GDA
        GenericDeviceActionContext ac = invokeAction(myGenericDevice, "CustomService", "CustomAction", "CustomArgument", 47);

        assertEquals("result=47", ac.getResult().getValue().getStringValue("CustomResult"));
    }

    
    /**
     * @param device
     * @param serviceName
     * @param actionName
     * @param parameterName
     * @return
     * @throws GenericDeviceException
     */
    private GenericDeviceActionContext invokeAction(SchemaBasedGenericDevice device, final String serviceName, final String actionName, final String parameterName, int arg) throws GenericDeviceException {
        GenericDeviceAction action = device.getService(serviceName).getAction(actionName);
        GenericDeviceActionContext ac = action.createActionContext();
        ac.setDevice(device.getName());
        ac.setService(serviceName);
        ac.setAction(actionName);

        GenericDeviceProperties args = ac.getArguments();
        args.setIntValue(parameterName, arg);

        action.execute(ac);
        return ac;
    }
}
