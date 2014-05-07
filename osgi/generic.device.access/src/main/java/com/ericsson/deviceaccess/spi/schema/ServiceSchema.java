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
