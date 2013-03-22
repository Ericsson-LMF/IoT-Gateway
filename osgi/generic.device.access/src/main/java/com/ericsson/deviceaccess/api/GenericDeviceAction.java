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
 * GenericDeviceAction represents an action of a service provided by a device.
 * The action can be executed in a GenericDeviceActionContext specified at the execution time, 
 * or the action can be executed without any action context.
 * 
 */
public interface GenericDeviceAction extends GenericDeviceContextNode {
    /**
     * Placeholder for Android to replace with the stub implementation for this
     * interface
     *
     * @author knt
     */
    public static abstract class Stub implements GenericDeviceAction {
    }

    /**
     * Getter for the name of the action.
     *
     * @return Name of the action.
     */
    public String getName();

    /**
     * Method to execute the action. An instance of GenericDeviceActionContext
     * is used to provide action context information such as requester of the
     * action, owner of the device, authorization status, arguments for the
     * action and so on. The GenericDeviceActionContext object is used for
     * propagating action result to the invoker.
     *
     * @param sac An instance of GenericDeviceActionContext which cannot be
     *            null. The object is used for propagting action result or error
     *            message.
     * @throws GenericDeviceException Thrown on failure of the action.
     * @deprecated use {@link #execute(GenericDeviceProperties)} instead.
     */
    public void execute(GenericDeviceActionContext sac)
            throws GenericDeviceException;

    /**
     * Alternative method to execute the action without context.
     *
     * @param arguments
     * @return the result
     * @throws GenericDeviceException Thrown on failure of the action.
     */
    public GenericDeviceActionResult execute(GenericDeviceProperties arguments)
            throws GenericDeviceException;

    /**
     * Creates arguments to be used in an invocation of this action. It is
     * populated with the valid arguments according to the schema of this
     * action.
     *
     * @return a fresh properties pre-populated with default values according to
     *         the schema.
     */
    public GenericDeviceProperties createArguments();

    /**
     * Creates a context to be used in an invocation of this action.
     *
     * @return a fresh context
     * @deprecated use {@link #execute(GenericDeviceProperties)} instead, then no context is needed.
     */
    public GenericDeviceActionContext createActionContext();

    /**
     * Gets metadata describing the result.
     *
     * @return metadata describing the result
     */
    public GenericDevicePropertyMetadata[] getResultMetadata();

    /**
     * Gets metadata describing the arguments.
     *
     * @return metadata describing the arguments
     */
    public GenericDevicePropertyMetadata[] getArgumentsMetadata();
}
