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
package com.ericsson.deviceaccess.api;

/**
 * A result from an action.
 */
public interface GenericDeviceActionResult extends Serializable {
    /**
     * Placeholder for Android to replace with the stub implementation for this
     * interface
     *
     * @author knt
     */
    public static abstract class Stub implements GenericDeviceActionResult {
    }

    /**
     * Gets the status code.
     *
     * @return
     */
    public int getCode();

    /**
     * Sets the status code.
     *
     * @param code
     */
    public void setCode(int code);

    /**
     * Gets the reason (associated with code).
     *
     * @return
     */
    public String getReason();

    /**
     * Sets the reason (associated with code).
     *
     * @param reason
     */
    public void setReason(String reason);

    /**
     * Gets the result value.
     *
     * @return
     */
    public GenericDeviceProperties getValue();
}
