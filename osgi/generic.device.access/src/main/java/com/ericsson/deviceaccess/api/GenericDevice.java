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

public interface GenericDevice extends GenericDeviceContextNode {

    /**
     * Non-mandatory transient state to signal that the device has been
     * added/paired
     */
    public static final String STATE_ADDED = "Added";

    /**
     * Non-mandatory state to indicate that the device is being initialized
     * (typically after a pairing)
     */
    public static final String STATE_STARTING = "Starting";

    /**
     * Default and mandatory value to indicate that the device is ready to be
     * used
     */
    public static final String STATE_READY = "Ready";

    /**
     * State to indicate that the device has failed to initialize
     */
    public static final String STATE_FAILED = "Failed";

    /**
     * Non-mandatory transient state to signal that the device has been
     * permanently removed/unpaired
     */
    public static final String STATE_REMOVED = "Removed";

    /**
     * Placeholder for Android to replace with the stub implementation for this
     * interface
     *
     * @author knt
     */
    public static abstract class Stub implements GenericDevice {

    }

    /**
     * Sets the name of this device.
     *
     * @param name
     */
    public void setName(String name);

    /**
     * Method to query a service offered by the device by its name.
     *
     * @param name Name of the service in question.
     * @return GenericDeviceService object if the device offers the queried
     * service. Null otherwise.
     */
    public GenericDeviceService getService(String name);

    /**
     * Method to return an array of service names to iterate services offered by
     * the device.
     *
     * @return array of service names
     */
    public String[] getServiceNames();

    /**
     * Getter for the device ID field. This is a gateway local ID.
     *
     * @return Device id.
     */
    public String getId();

    /**
     * Sets the URN of this device. The URN will be persisted.
     *
     * @param URN
     */
    public void setURN(String URN);

    /**
     * Gets the URN of this device.
     *
     * @return
     */
    public String getURN();

    /**
     * Getter the friendly name of the device.
     *
     * @return Friendly name of the device.
     */
    public String getName();

    /**
     * Getter for the device type field.
     *
     * @return Device type.
     */
    public String getType();

    /**
     * Getter for the name of protocol used for discovery of the device. The
     * value should be one of constant properties defined in
     * com.ericsson.deviceaccess.api.Constants that start with prefix "PROTO_".
     *
     * @return Name of the protocol used for discovery of the device.
     */
    public String getProtocol();

    /**
     * Getter for location where the device is discovered.
     *
     * @return Location where the device is discovered.
     */
    public String getLocation();

    /**
     * Getter for the boolean field that indicates if the device is online, i.e.
     * can be communicated through the GenericDevice API or not.
     *
     * @return true if the device is online. false otherwise.
     */
    public boolean isOnline();

    /**
     * Gets the running state of the device:
     * {@link STATE_ADDED}, {@link STATE_REMOVED}, {@link #STATE_READY} or
     * {@link #STATE_STARTING}.
     *
     * @return
     */
    public String getState();

    /**
     * Getter for the icon url field. The value should be URL for an icon image
     * of the device that is available for the clients to fetch, i.e. image file
     * on an HTTP server.
     *
     * @return URL for an icon image of the device.
     */
    public String getIcon();

    /**
     * @return Contact URL of the device.
     * @deprecate Getter for contact URL of the device that is used in Web
     * Device Connectivity. This method should be removed because each
     * application should have its own way of exposing the device and thus the
     * contact URL of the device depends.
     */
    public String getContact();

    /**
     * Getter for name of the manufactuer of the device.
     *
     * @return name of the manufacturer. Null if the manufacturer is unknown.
     */
    public String getManufacturer();

    /**
     * Getter for the description of the device.
     *
     * @return description of the device. Null if no description is available.
     */
    public String getDescription();

    /**
     * Getter for serial number of the device.
     *
     * @return Serial number of the device. Null if unknown.
     */
    public String getSerialNumber();

    /**
     * Getter for product class of the device.
     *
     * @return product class of the device. Null if unknown.
     */
    public String getProductClass();

    /**
     * Getter for model name of the device
     *
     * @return model name of the device. Null if the model name is unknown.
     */
    public String getModelName();

    /**
     * Serializes the state (i.e. values of all properties in all services) to
     * JSON
     *
     * @return JSON of the state. Example:
     * <code>{"Service1" : {"property1" : "99","property2" : "99"},"Service2" : {"property3" : "99","property4" : "99"}}</code>
     * @throws com.ericsson.deviceaccess.api.GenericDeviceException
     */
    public String serializeState() throws GenericDeviceException;
}
