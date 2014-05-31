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
 *
 */
public class ActionSchema {

    private String name;
    private List argumentSchemas = new ArrayList();
    private List resultSchemas = new ArrayList();
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
    public ParameterSchema[] getArgumentsSchemas() {
        return (ParameterSchema[]) argumentSchemas.toArray(new ParameterSchema[argumentSchemas.size()]);
    }

    /**
     * Gets the schemas of the arguments of this action.
     *
     * @return the arguments of this action
     */
    public ParameterSchema[] getResultSchema() {
        return (ParameterSchema[]) resultSchemas.toArray(new ParameterSchema[resultSchemas.size()]);
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
        private List argumentSchemas = new ArrayList();
        private List resultSchemas = new ArrayList();
        private boolean isMandatory;

        /**
         * Create builder for action with the specified name.
         *
         * @param actionName
         */
        public Builder(String actionName) {
            super();
            this.name = actionName;
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
         * @param ParameterSchema
         * @return the builder
         */
        public Builder addArgumentSchema(ParameterSchema argumentSchema) {
            argumentSchemas.add(argumentSchema);
            return this;
        }

        /**
         * Adds a result to the action schema.
         *
         * @param ParameterSchema
         * @return the builder
         */
        public Builder addResultSchema(ParameterSchema resultSchema) {
            resultSchemas.add(resultSchema);
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
            for (Iterator iterator = argumentSchemas.iterator(); iterator.hasNext();) {
                ParameterSchema argumentSchema = (ParameterSchema) iterator.next();
                actionSchema.argumentSchemas.add(argumentSchema);
            }
            for (Iterator iterator = resultSchemas.iterator(); iterator.hasNext();) {
                ParameterSchema resultSchema = (ParameterSchema) iterator.next();
                actionSchema.resultSchemas.add(resultSchema);
            }
            return actionSchema;
        }
    }
}
