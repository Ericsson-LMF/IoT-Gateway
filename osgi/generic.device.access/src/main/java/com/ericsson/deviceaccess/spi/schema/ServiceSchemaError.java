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

/**
 * Error thrown when instantiating a service from a schema.  
 */
public class ServiceSchemaError extends Error {

	private static final long serialVersionUID = 1L;

	public ServiceSchemaError() {
    }

    public ServiceSchemaError(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceSchemaError(String message) {
        super(message);
    }

    public ServiceSchemaError(Throwable cause) {
        super(cause);
    }
}
