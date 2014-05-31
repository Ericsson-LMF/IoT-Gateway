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
package com.ericsson.deviceaccess.api;

/**
 * GenericDeviceAction represents an action of a service provided by a device.
 * The action can be executed in a GenericDeviceActionContext specified at the
 * execution time, or the action can be executed without any action context.
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
     * null. The object is used for propagting action result or error message.
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
     * the schema.
     */
    public GenericDeviceProperties createArguments();

    /**
     * Creates a context to be used in an invocation of this action.
     *
     * @return a fresh context
     * @deprecated use {@link #execute(GenericDeviceProperties)} instead, then
     * no context is needed.
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
