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
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH
 DAMAGES.
 *
 */
package com.ericsson.deviceaccess.spi.schema.based;

import com.ericsson.deviceaccess.api.genericdevice.GDService;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceImpl;
import com.ericsson.deviceaccess.spi.schema.DeviceManagementService;
import com.ericsson.deviceaccess.spi.schema.ServiceSchema;
import com.ericsson.deviceaccess.spi.schema.ServiceSchemaError;
import java.util.HashMap;

/**
 * Base class for devices implemented in adaptors.
 */
public abstract class SBGenericDevice extends GenericDeviceImpl {

    protected SBGenericDevice() {
        addSchemaBasedService(new DeviceManagementService());
    }

    /**
     * Adds a schema based service to this device.
     *
     * @param service
     */
    protected final void addSchemaBasedService(SBService service) {
        String name = service.getName();
        if (getService(name) != null) {
            throw new ServiceSchemaError("Service: '" + name + "' is already defined on the device: '" + getName() + "'(id=" + getId() + ")");
        }
        service.validateSchema();
        super.putService(service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void putService(GDService svc) {
        if (!(svc instanceof SBService)) {
            throw new ServiceSchemaError("Trying to add a service '" + svc.getName() + "', on the device: '" + getName() + "'(id=" + getId() + "), which is not a " + SBService.class);
        }
        addSchemaBasedService((SBService) svc);
    }

    /**
     * Sets the services of this device.
     *
     * @param services
     */
    public void setService(HashMap<Object, GDService> services) {
        services.values().forEach(this::putService);
    }

    /**
     * Creates a custom service.
     *
     * @param schema the service schema
     * @return the service
     */
    public SBService createService(ServiceSchema schema) {
        return new SBServiceBase(schema);
    }
}
