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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Implementation of a service schema.
 */
public class ServiceSchema {

    private String name;
    private List actionSchemas = new ArrayList();
    private List propertySchemas = new ArrayList();

    /**
     * Creates schema with specified name.
     * 
     * @param name
     */
    private ServiceSchema(String name) {
        this.name = name;
    }

    /**
     * Returns the name of the service.
     * @return the name of the service
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the action schemas defined for this schema.
     * @return the actions defined for this schema
     */
    public ActionSchema[] getActionSchemas() {
        return (ActionSchema[]) actionSchemas.toArray(new ActionSchema[actionSchemas.size()]);
    }

    /**
     * Gets the parameters for this service
     * @return
     */
    public ParameterSchema[] getPropertiesSchemas() {
        return (ParameterSchema[]) propertySchemas.toArray(new ParameterSchema[propertySchemas.size()]);
    }

    /**
     * Builder for creating ServiceSchema instances.
     */
    public static class Builder {
        private String name;
        private List actionSchemas = new ArrayList();
        private List propertieSchemas = new ArrayList();

        /**
         * Create builder for a service with the specified name.
         * @param serviceName
         * @return the builder
         */
        public Builder(String serviceName) {
            this.name = serviceName;
        }
        
        /**
         * Adds an action schema.
         * 
         * @param actionSchema
         * @return the builder
         */
        public Builder addActionSchema(ActionSchema actionSchema) {
            actionSchemas.add(actionSchema);
            return this;
        }
        
        /**
         * Adds a parameter schema.
         * @param parameterSchema
         * @return the builder
         */
        public Builder addPropertySchema(ParameterSchema parameterSchema) {
            propertieSchemas.add(parameterSchema);
            return this;
        }
        
        /**
         * Builds the schema.
         * @return the built schema
         */
        public ServiceSchema build() {
            if (name == null) {
                throw new ServiceSchemaError("Name must be specified");
            }

            ServiceSchema serviceSchema = new ServiceSchema(name);
            for (Iterator iterator = actionSchemas.iterator(); iterator.hasNext();) {
                ActionSchema actionSchema = (ActionSchema) iterator.next();
                serviceSchema.actionSchemas.add(actionSchema);
            }
            
            for (Iterator iterator = propertieSchemas.iterator(); iterator.hasNext();) {
                ParameterSchema propertySchema = (ParameterSchema) iterator.next();
                serviceSchema.propertySchemas.add(propertySchema);
            }
            
            return serviceSchema;
        }
    }
}
