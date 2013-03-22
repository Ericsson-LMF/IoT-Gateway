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
 * The context for an action invocation.
 */
public interface GenericDeviceActionContext extends Serializable {
    /**
     * Placeholder for Android to replace with the stub implementation for this interface
     *
     * @author knt
     */
    public static abstract class Stub implements GenericDeviceActionContext {
    }

    /**
     * Sets the name if the entity requesting the action.
     *
     * @param requester
     */
    public void setRequester(String requester);

    /**
     * Gets the name if the entity requesting the action.
     *
     * @return
     */
    public String getRequester();

    /**
     * Sets the device that the action concerns.
     *
     * @param device
     */
    public void setDevice(String device);

    /**
     * Gets the device that the action concerns.
     *
     * @return
     */
    public String getDevice();

    /**
     * Sets the service that the action concerns.
     *
     * @param service
     */
    public void setService(String service);

    /**
     * Gets the service that the action concerns.
     *
     * @return
     */
    public String getService();

    /**
     * Sets the owner of the device.
     *
     * @param owner
     */
    public void setOwner(String owner);

    /**
     * Gets the owner of the device.
     *
     * @return
     */
    public String getOwner();

    /**
     * Sets the service that the action concerns.
     *
     * @param action
     */
    public void setAction(String action);

    /**
     * Gets the service that the action concerns.
     *
     * @return
     */
    public String getAction();

    /**
     * Sets whether the action is authorized or not.
     *
     * @param isAuthorized
     */
    public void setAuthorized(boolean isAuthorized);

    /**
     * Gets whether the action is authorized or not.
     *
     * @return
     */
    public boolean isAuthorized();

    /**
     * ??
     *
     * @param isFirstTime
     */
    public void setFirstTime(boolean isFirstTime);

    /**
     * ??
     *
     * @return
     */
    public boolean isFirstTime();

    /**
     * Gets the result from the action.
     *
     * @return
     */
    public GenericDeviceActionResult getResult();

    /**
     * ??
     *
     * @param requesterContact
     */
    public void setRequesterContact(String requesterContact);

    /**
     * ??
     *
     * @return
     */
    public String getRequesterContact();

    /**
     * Gets the arguments of the action.
     *
     * @return
     */
    public GenericDeviceProperties getArguments();
}
