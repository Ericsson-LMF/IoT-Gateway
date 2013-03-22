package com.ericsson.deviceaccess.spi.schema;

import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDevicePropertyMetadata;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceActionImpl;

final class SchemaBasedAction extends
		GenericDeviceActionImpl {
	private SchemaBasedServiceBase schemaBasedServiceBase;

	SchemaBasedAction(String name, 
			SchemaBasedServiceBase schemaBasedServiceBase,
			GenericDevicePropertyMetadata[] argumentsMetadata,
			GenericDevicePropertyMetadata[] resultMetadata) {
		super(name, argumentsMetadata, resultMetadata);
		this.schemaBasedServiceBase = schemaBasedServiceBase;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Executes the action definition (added with
	 * {@link SchemaBasedServiceBase#defineAction(String, ActionDefinition)}
	 * ).
	 */
	public void execute(GenericDeviceActionContext sac) throws GenericDeviceException {
	    ActionDefinition actionDefinition = (ActionDefinition) schemaBasedServiceBase.getActionDefinitions(getName());
	    if (actionDefinition == null) {
	    } else {
	        actionDefinition.invoke(sac);
	    }
	}
}