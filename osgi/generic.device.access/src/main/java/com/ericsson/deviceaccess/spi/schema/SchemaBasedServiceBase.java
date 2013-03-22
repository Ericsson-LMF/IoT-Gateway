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
import java.util.Map;

import com.ericsson.deviceaccess.spi.impl.GenericDeviceActionImpl;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceServiceImpl;

/**
 * A service implementation based on a schema.
 */
public class SchemaBasedServiceBase extends GenericDeviceServiceImpl implements SchemaBasedService {

    public static final String REFRESH_PROPERTIES = "refreshProperties";
    private ServiceSchema serviceSchema;
    private Map actionDefinitions = new HashMap();

    /**
     * Creates instance based on specified schema.
     * 
     * @param serviceSchema the schema
     */
    public SchemaBasedServiceBase(ServiceSchema serviceSchema) {
        super(serviceSchema.getName(), serviceSchema.getPropertiesSchemas());
        this.serviceSchema = serviceSchema;
        init(serviceSchema);
    }

    /**
     * {@inheritDoc}
     */
    public final SchemaBasedService defineAction(String name, ActionDefinition actionDefinition) {
        if (getAction(name) == null) {
            throw new ServiceSchemaError("The action: '" + name + "' is not specified in the service schema");
        }
        
        if (actionDefinitions.containsKey(name)) {
            throw new ServiceSchemaError("The action: '" + name + "' has already been defined");
        }
        
        actionDefinitions.put(name, actionDefinition);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public final SchemaBasedService defineCustomAction(final ActionSchema actionSchema, ActionDefinition actionDefinition) {
        String name = actionSchema.getName();
        if (getAction(name) != null) {
            throw new ServiceSchemaError("The action: '" + name + "' is already defined in the service schema");
        }

        // This is an action not defined in the schema.
        createAction(actionSchema);

        actionDefinitions.put(name, actionDefinition);
        
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public final void validateSchema() {
        ActionSchema[] actionSchemas = serviceSchema.getActionSchemas();
        for (int i = 0; i < actionSchemas.length; i++) {
            ActionSchema action = actionSchemas[i];
            String name = action.getName();
            if (action.isMandatory() && !actionDefinitions.containsKey(name)) {
                throw new ServiceSchemaError("The action: '"+name+"' in service: '"+getName()+"' is mandatory, but lacks definition.");
            }
        }
    }
    
    /**
     * Creates the actions and parameters based on the specified schema.
     * 
     * @param serviceSchema
     */
    private void init(ServiceSchema serviceSchema) {
        ActionSchema[] actionSchemas = serviceSchema.getActionSchemas();
        for (int i = 0; i < actionSchemas.length; i++) {
            final ActionSchema actionSchema = actionSchemas[i];
            createAction(actionSchema);
        }

        createAction(new ActionSchema.Builder(REFRESH_PROPERTIES).setMandatory(true).build());

        ParameterSchema[] ParameterSchemaImpls = serviceSchema.getPropertiesSchemas();
        for (int i = 0; i < ParameterSchemaImpls.length; i++) {
            final ParameterSchema ParameterSchemaImpl = ParameterSchemaImpls[i];
            getProperties().setStringValue(ParameterSchemaImpl.getName(), ParameterSchemaImpl.getDefaultStringValue());
        }
    }

    /**
     * @param actionSchema
     */
    private void createAction(final ActionSchema actionSchema) {
        String name = actionSchema.getName();
        final ParameterSchema[] argumentsSchemas = actionSchema.getArgumentsSchemas();
        final ParameterSchema[] resultParametersSchemas = actionSchema.getResultSchema();
        GenericDeviceActionImpl genericDeviceActionImpl = new SchemaBasedAction(name, this, argumentsSchemas, resultParametersSchemas);
        putAction(genericDeviceActionImpl);
    }

	ActionDefinition getActionDefinitions(String name) {
		return (ActionDefinition) actionDefinitions.get(name);
	}
}
