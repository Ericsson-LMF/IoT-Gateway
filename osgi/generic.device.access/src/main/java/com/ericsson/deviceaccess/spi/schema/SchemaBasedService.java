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

import com.ericsson.deviceaccess.spi.GenericDeviceService;


/**
 * 
 */
public interface SchemaBasedService extends GenericDeviceService {
    /**
     * Add action implementation to the service.
     * @param name action name
     * @param actionDefinition the action implementation
     * @return the service
     */
    SchemaBasedService defineAction(String name, ActionDefinition actionDefinition);
    
    /**
     * Defines a custom action that is not included in the service schema.
     * @param actionSchema
     * @param actionDefinition
     * @return
     */
    SchemaBasedService defineCustomAction(ActionSchema actionSchema, ActionDefinition actionDefinition);

    /**
     * Verifies that the schema is valid.
     * 
     * @throws in case the schema is invalid
     */
    void validateSchema() throws ServiceSchemaError;
}
