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
