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
 *
 */
public class ActionSchema {

    private String name;
    private List<ParameterSchema> argumentSchemas = new ArrayList<>();
    private List<ParameterSchema> resultSchemas = new ArrayList<>();
    private boolean isMandatory;

    /**
     * Creates an instance.
     *
     * @param name
     * @param isMandatory
     */
    private ActionSchema(String name, boolean isMandatory) {
        this.isMandatory = isMandatory;
        this.name = name;
    }

    /**
     * Returns the name of the action.
     *
     * @return the name of the action
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the schemas of the arguments of this action.
     *
     * @return the arguments of this action
     */
    public List<ParameterSchema> getArgumentsSchemas() {
        return argumentSchemas;
    }

    /**
     * Gets the schemas of the arguments of this action.
     *
     * @return the arguments of this action
     */
    public List<ParameterSchema> getResultSchema() {
        return resultSchemas;
    }

    /**
     * Indicates that this action must be implemented.
     *
     * @return
     */
    public boolean isMandatory() {
        return isMandatory;
    }

    /**
     * Builder for building action schemas.
     */
    public static class Builder {

        private String name;
        private final List<ParameterSchema> argumentSchemas = new ArrayList<>();
        private final List<ParameterSchema> resultSchemas = new ArrayList<>();
        private boolean isMandatory;

        /**
         * Create builder for action with the specified name.
         *
         */
        public Builder() {
            super();
        }

        public Builder(String name) {
            this.name = name;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        /**
         * Specify that the action is mandatory.
         *
         * @param mandatory
         * @return the builder
         */
        public Builder setMandatory(boolean mandatory) {
            isMandatory = mandatory;
            return this;
        }

        /**
         * Adds an argument to the action schema.
         *
         * @param consumer
         * @return the builder
         */
        public Builder addArgument(Consumer<ParameterSchema.Builder> consumer) {
            ParameterSchema.Builder builder = new ParameterSchema.Builder();
            consumer.accept(builder);
            argumentSchemas.add(builder.build());
            return this;
        }

        public Builder addArgument(ParameterSchema argument) {
            argumentSchemas.add(argument);
            return this;
        }

        public Builder addArgument(String name, Class<?> type) {
            argumentSchemas.add(new ParameterSchema.Builder(name, type).build());
            return this;
        }

        /**
         * Adds a result to the action schema.
         *
         * @param consumer
         * @return the builder
         */
        public Builder addResult(Consumer<ParameterSchema.Builder> consumer) {
            ParameterSchema.Builder builder = new ParameterSchema.Builder();
            consumer.accept(builder);
            resultSchemas.add(builder.build());
            return this;
        }

        public Builder addResult(ParameterSchema result) {
            resultSchemas.add(result);
            return this;
        }

        public Builder addResult(String name, Class<?> type) {
            argumentSchemas.add(new ParameterSchema.Builder(name, type).build());
            return this;
        }

        /**
         * Builds the schema.
         *
         * @return the build schema.
         */
        public ActionSchema build() {
            if (name == null) {
                throw new ServiceSchemaError("Name must be specified");
            }
            ActionSchema actionSchema = new ActionSchema(name, isMandatory);
            argumentSchemas.forEach(actionSchema.argumentSchemas::add);
            resultSchemas.forEach(actionSchema.resultSchemas::add);
            return actionSchema;
        }
    }
}
