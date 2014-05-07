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
