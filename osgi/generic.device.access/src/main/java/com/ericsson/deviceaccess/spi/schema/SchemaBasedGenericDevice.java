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
