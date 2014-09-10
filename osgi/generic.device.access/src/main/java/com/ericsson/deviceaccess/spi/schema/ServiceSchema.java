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
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of a service schema.
 */
public class ServiceSchema {

    private String name;
    private List<ActionSchema> actionSchemas = new ArrayList<>();
    private List<ParameterSchema> propertySchemas = new ArrayList<>();

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
     *
     * @return the name of the service
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the action schemas defined for this schema.
     *
     * @return the actions defined for this schema
     */
    public List<ActionSchema> getActionSchemas() {
        return actionSchemas;
    }

    /**
     * Gets the parameters for this service
     *
     * @return
     */
    public List<ParameterSchema> getPropertiesSchemas() {
        return propertySchemas;
    }

    /**
     * Builder for creating ServiceSchema instances.
     */
    public static class Builder {

        private String name;
        private final List<ActionSchema> actionSchemas = new ArrayList<>();
        private final List<ParameterSchema> propertieSchemas = new ArrayList<>();

        /**
         * Create builder for a service with the specified name.
         *
         */
        public Builder() {
        }

        public Builder(String name) {
            this.name = name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Adds an action schema.
         *
         * @param consumer
         * @return the builder
         */
        public Builder addAction(Consumer<ActionSchema.Builder> consumer) {
            ActionSchema.Builder builder = new ActionSchema.Builder();
            consumer.accept(builder);
            actionSchemas.add(builder.build());
            return this;
        }

        public Builder addAction(ActionSchema action) {
            actionSchemas.add(action);
            return this;
        }

        public Builder addAction(String name) {
            actionSchemas.add(new ActionSchema.Builder(name).build());
            return this;
        }

        /**
         * Adds a parameter schema.
         *
         * @param consumer
         * @return the builder
         */
        public Builder addProperty(Consumer<ParameterSchema.Builder> consumer) {
            ParameterSchema.Builder builder = new ParameterSchema.Builder();
            consumer.accept(builder);
            propertieSchemas.add(builder.build());
            return this;
        }

        public Builder addProperty(ParameterSchema parameter) {
            propertieSchemas.add(parameter);
            return this;
        }

        public Builder addProperty(String name, Class<?> type) {
            propertieSchemas.add(new ParameterSchema.Builder(name, type).build());
            return this;
        }

        /**
         * Builds the schema.
         *
         * @return the built schema
         */
        public ServiceSchema build() {
            if (name == null) {
                throw new ServiceSchemaError("Name must be specified");
            }
            ServiceSchema serviceSchema = new ServiceSchema(name);
            actionSchemas.forEach(serviceSchema.actionSchemas::add);
            propertieSchemas.forEach(serviceSchema.propertySchemas::add);
            return serviceSchema;
        }
    }
}
