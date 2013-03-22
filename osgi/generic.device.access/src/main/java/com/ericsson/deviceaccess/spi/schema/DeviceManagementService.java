/**
 * User: Joel Binnquist (joel.binnquist@gmail.com)
 * Date: 2011-12-15
 * Time: 20:47
 */
package com.ericsson.deviceaccess.spi.schema;

import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
import com.ericsson.deviceaccess.api.GenericDeviceException;

/**
 * Implementation of the device management service.
 */
public class DeviceManagementService extends SchemaBasedServiceBase {
    public static final String SET_NAME = "setName";
    public static final String DEVICE_MANAGEMENT = "DeviceManagement";
    public static final String NEW_NAME = "newName";
    public static final String SET_URN = "setURN";
    public static final String NEW_URN = "newURN";
    private static final ServiceSchema SERVICE_SCHEMA =
        new ServiceSchema.Builder(DEVICE_MANAGEMENT)
            .addActionSchema(new ActionSchema.Builder(SET_NAME)
                .addArgumentSchema(new ParameterSchema.Builder(NEW_NAME)
                    .setType(String.class)
                    .build())
                .build())
            .addActionSchema(new ActionSchema.Builder(SET_URN)
                .addArgumentSchema(new ParameterSchema.Builder(NEW_URN)
                    .setType(String.class)
                    .build())
                .build())
            .build();

    /**
     * Creates instance.
     */
    public DeviceManagementService() {
        super(SERVICE_SCHEMA);
        defineAction(SET_NAME, new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String newName = context.getArguments().getStringValue(NEW_NAME);
                getParentDevice().setName(newName);
            }
        });
        defineAction(SET_URN, new ActionDefinition() {
            public void invoke(GenericDeviceActionContext context) throws GenericDeviceException {
                String newUrn = context.getArguments().getStringValue(NEW_URN);
                getParentDevice().setURN(newUrn);
            }
        });
    }
}
