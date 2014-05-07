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

import java.util.List;

/**
 * This interface is intended to be used when using
 * GDA on non-OSGi platform, e.g. Android.
 * It mimics OSGi service framework interface without
 * dependency to OSGi specific API.
 *
 * @author ekenyas
 */
public interface GenericDeviceFramework {

    /**
     * This method should be called when a new device is discovered
     * and to be registered. It corresonds to
     *
     * @param dev The device to be registered
     * @throws GenericDeviceException
     */
    public void register(GenericDevice dev) throws GenericDeviceException;

    /**
     * This method should be called when an update occurs on the
     * concerning device, e.g. update on sensor reading.
     *
     * @param dev          The concerned device
     * @param updatedPaths Comma separated list of paths that the event concerns.
     * @throws GenericDeviceException
     */
    public void update(GenericDevice dev, String updatedPaths) throws GenericDeviceException;

    /**
     * Method to be called when the concerned device gets unavailable
     * and should be removed from the framework
     *
     * @param dev the device to be unregistered.
     * @throws GenericDeviceException
     */
    public void unregister(GenericDevice dev) throws GenericDeviceException;

    /**
     * Method to get the list of all the devices registered to the framework
     */
    List getAllDevices();

    /**
     * Method to get a device registered in the framework.
     *
     * @param id The ID of the device in question
     * @return GenericDevice instance that has the specified id. Returns null
     *         if no such device is found.
     */
    GenericDevice getDevice(String id);

    /**
     * Method to be called for adding a listener for device discovery events.
     *
     * @param listener
     */
    public void addListener(GenericDeviceFrameworkListener listener);

    /**
     * Method to be called when removing a listener for device discovery events.
     *
     * @param listener
     */
    public void removeListener(GenericDeviceFrameworkListener listener);


}
