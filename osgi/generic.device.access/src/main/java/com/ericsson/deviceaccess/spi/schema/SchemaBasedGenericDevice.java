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

package com.ericsson.deviceaccess.spi.schema;

import java.util.HashMap;
import java.util.Iterator;

import com.ericsson.deviceaccess.api.GenericDeviceService;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceImpl;

/**
 * Base class for devices implemented in adaptors.
 */
public abstract class SchemaBasedGenericDevice extends GenericDeviceImpl {
    protected SchemaBasedGenericDevice() {
        addSchemaBasedService(new DeviceManagementService());
    }

    /**
     * Adds a schema based service to this device.
     * 
     * @param service
     */
    protected void addSchemaBasedService(SchemaBasedService service) {
        String name = service.getName();
        if (getService(name) != null) {
            throw new ServiceSchemaError("Service: '" + name + "' is already defined on the device: '" + getName() + "'(id=" + getId() + ")");
        }
        
        service.validateSchema();
        super.putService(name, service);
    }
    
    
    
    /**
     * {@inheritDoc}
     */
    public void putService(GenericDeviceService svc) {
        putService(null, svc);
    }

    /**
     * {@inheritDoc}
     */
    public void putService(String name, GenericDeviceService svc) {
        if (!(svc instanceof SchemaBasedService)) {
            throw new ServiceSchemaError("Trying to add a service '" + name + "', on the device: '" + getName() + "'(id=" + getId() + "), which is not a "+SchemaBasedService.class);
        }
        addSchemaBasedService((SchemaBasedService)svc);
    }

    /**
     * Sets the services of this device.
     */
    public void setService(HashMap services) {
        for (Iterator iterator = services.values().iterator(); iterator.hasNext();) {
            GenericDeviceService service = (GenericDeviceService) iterator.next();
            putService(service); 
        } 
    }



    /**
     * Creates a custom service.
     * 
     * @param schema the service schema
     * @return the service
     */
    public SchemaBasedService createService(ServiceSchema schema) {
        String name = schema.getName();
        SchemaBasedServiceBase service = new SchemaBasedServiceBase(schema);
        return service;
    }
}
