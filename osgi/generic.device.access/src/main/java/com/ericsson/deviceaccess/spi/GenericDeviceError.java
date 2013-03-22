/*
 * Copyright (c) Ericsson AB, 2013.
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

package com.ericsson.deviceaccess.spi;

/**
 * An error (typically programming error).
 */
public class GenericDeviceError extends Error {

	private static final long serialVersionUID = 1L;

    public GenericDeviceError() {
    }

    /**
     * @param message
     * @param cause
     */
    public GenericDeviceError(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public GenericDeviceError(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public GenericDeviceError(Throwable cause) {
        super(cause);
    }
}
