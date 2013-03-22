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
         * @param actionName
         */
        public Builder(String actionName) {
            super();
            this.name = actionName;
        }

        /**
         * Specify that the action is mandatory.
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
