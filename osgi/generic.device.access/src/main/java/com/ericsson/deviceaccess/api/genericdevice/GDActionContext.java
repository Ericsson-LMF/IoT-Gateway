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
package com.ericsson.deviceaccess.api.genericdevice;

import com.ericsson.deviceaccess.api.Serializable;

/**
 * The context for an action invocation.
 */
public interface GDActionContext extends Serializable {

    /**
     * Placeholder for Android to replace with the stub implementation for this
     * interface
     *
     * @author knt
     */
    public static abstract class Stub implements GDActionContext {
    }

    /**
     * Sets the name if the entity requesting the action.
     *
     * @param requester
     */
    void setRequester(String requester);

    /**
     * Gets the name if the entity requesting the action.
     *
     * @return
     */
    String getRequester();

    /**
     * Sets the device that the action concerns.
     *
     * @param device
     */
    void setDevice(String device);

    /**
     * Gets the device that the action concerns.
     *
     * @return
     */
    String getDevice();

    /**
     * Sets the service that the action concerns.
     *
     * @param service
     */
    void setService(String service);

    /**
     * Gets the service that the action concerns.
     *
     * @return
     */
    String getService();

    /**
     * Sets the owner of the device.
     *
     * @param owner
     */
    void setOwner(String owner);

    /**
     * Gets the owner of the device.
     *
     * @return
     */
    String getOwner();

    /**
     * Sets the service that the action concerns.
     *
     * @param action
     */
    void setAction(String action);

    /**
     * Gets the service that the action concerns.
     *
     * @return
     */
    String getAction();

    /**
     * Sets whether the action is authorized or not.
     *
     * @param isAuthorized
     */
    void setAuthorized(boolean isAuthorized);

    /**
     * Gets whether the action is authorized or not.
     *
     * @return
     */
    boolean isAuthorized();

    /**
     * ??
     *
     * @param isFirstTime
     */
    void setFirstTime(boolean isFirstTime);

    /**
     * ??
     *
     * @return
     */
    boolean isFirstTime();

    /**
     * Gets the result from the action.
     *
     * @return
     */
    GDActionResult getResult();

    /**
     * ??
     *
     * @param requesterContact
     */
    void setRequesterContact(String requesterContact);

    /**
     * ??
     *
     * @return
     */
    String getRequesterContact();

    /**
     * Gets the arguments of the action.
     *
     * @return
     */
    GDProperties getArguments();
}
