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


public interface GenericDevice extends GenericDeviceContextNode {
    /**
     * Non-mandatory transient state to signal that the device has been added/paired
     */
    public static final String STATE_ADDED = "Added";

    /**
     * Non-mandatory state to indicate that the device is being initialized (typically after a pairing)
     */
	public static final String STATE_STARTING = "Starting";
	
	/**
	 * Default and mandatory value to indicate that the device is ready to be used
	 */
    public static final String STATE_READY = "Ready";
    
    /**
     * State to indicate that the device has failed to initialize
     */
    public static final String STATE_FAILED = "Failed";
    
    /**
     * Non-mandatory transient state to signal that the device has been permanently removed/unpaired
     */
    public static final String STATE_REMOVED = "Removed";

    /**
     * Placeholder for Android to replace with the stub implementation for this interface
     *
     * @author knt
     */
    public static abstract class Stub implements GenericDevice {

    }

    /**
     * Sets the name of this device.
     */
    public void setName(String name);

    /**
     * Method to query a service offered by the device by its name.
     *
     * @param name Name of the service in question.
     * @return GenericDeviceService object if the device offers the queried service. Null otherwise.
     */
    public GenericDeviceService getService(String name);

    /**
     * Method to return an array of service names to iterate services
     * offered by the device.
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
     * @param URN
     */
    public void setURN(String URN);

    /**
     * Gets the URN of this device.
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
     * Getter for the name of protocol used for discovery of the device. The value should be
     * one of constant properties defined in com.ericsson.deviceaccess.api.Constants that
     * start with prefix "PROTO_".
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
     * Gets the running state of the device: {@link STATE_ADDED}, {@link STATE_REMOVED}, {@link #STATE_READY} or {@link #STATE_STARTING}.
     * @return
     */
    public String getState();

    /**
     * Getter for the icon url field. The value should be URL for an icon image of the device
     * that is available for the clients to fetch, i.e. image file on an HTTP server.
     *
     * @return URL for an icon image of the device.
     */
    public String getIcon();

    /**
     * @return Contact URL of the device.
     * @deprecate Getter for contact URL of the device that is used in Web Device Connectivity. This method
     * should be removed because each application should have its own way of exposing the device and
     * thus the contact URL of the device depends.
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
     * Serializes the state (i.e. values of all properties in all services) to JSON
     *
     * @return JSON of the state.
     *         Example:
     *         <code>{"Service1" : {"property1" : "99","property2" : "99"},"Service2" : {"property3" : "99","property4" : "99"}}</code>
     */
    public String serializeState() throws GenericDeviceException;
}
