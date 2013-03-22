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

import com.ericsson.deviceaccess.api.GenericDeviceService;

/**
 * This is the interface of a generic device to be used by service providers.
 */
public interface GenericDevice extends com.ericsson.deviceaccess.api.GenericDevice {
	public static final String ID = "id";
	public static final String URN = "urn";
	public static final String NAME = "name";
	public static final String TYPE = "type";
	public static final String PROTOCOL = "protocol";
	public static final String LOCATION = "location";
	public static final String ONLINE = "online";
	public static final String ICON = "icon";
	public static final String PATH = "path";
	public static final String CONTACT = "contact";
	public static final String MANUFACTURER = "manufacturer";
	public static final String MODEL_NAME = "modelName";
	public static final String DESCRIPTION = "description";
	public static final String SERIAL_NUMBER = "serialNumber";
	public static final String PRODUCT_CLASS = "productClass";

    /**
     * Method to put an instance that implements GenericDeviceService into the device object.
     *
     * @param svc An instance that implements the service to be put.
     */
    public void putService(GenericDeviceService svc);

    /**
     * Setter for the device ID field. Non-null value must be set.
     *
     * @param id Device ID.
     */
    public void setId(String id);

    /**
     * Setter for the friendly name of the device.
     *
     * @param name Friendly name of the device.
     */
    public void setName(String name);

    /**
     * Setter for the device type field.
     *
     * @param type Device type
     */
    public void setType(String type);

    /**
     * Setter for the name of protocol used for discovery of the device. The value should be chosen from the
     * constant properties defined in com.ericsson.deviceaccess.api.Constants that
     * start with prefix "PROTO_".
     *
     * @param protocol Name of the protocol used for discovery of the device.
     */
    public void setProtocol(String protocol);

    /**
     * Setter for location where the device is discovered.
     *
     * @param location Location where the device is discovered.
     */
    public void setLocation(String location);

    /**
     * Setter for the boolean field that indicates if the device is online, i.e.
     * can be communicated through the GenericDevice API or not.
     *
     * @param online true if the device is online. false otherwise.
     */
    public void setOnline(boolean online);

    /**
     * Setter for the icon url field. The value should be URL for an icon image of the device
     * that is available for the clients to fetch, i.e. image file on an HTTP server.
     *
     * @param icon URL for an icon image of the device.
     */
    public void setIcon(String icon);

    /**
     * @param contact Contact URL of the device.
     * @deprecate Setter for contact URL of the device that is used in Web Device Connectivity. This method
     * should be removed because each application should have its own way of exposing the device and
     * thus the contact URL of the device depends.
     */
    public void setContact(String contact);

    /**
     * Setter for name of the manufacturer of the device.
     *
     * @param manufacturer name of manufacturer of the device.
     */
    public void setManufacturer(String manufacturer);

    /**
     * Setter for model name of the device.
     *
     * @param modelName model name of the device.
     */
    public void setModelName(String modelName);

    /**
     * Setter for the description of the device.
     *
     * @param description description of the device.
     */
    public void setDescription(String description);

    /**
     * Setter for the serial number of the device.
     *
     * @param serialNumber Serial number of the device.
     */
    public void setSerialNumber(String serialNumber);

    /**
     * Setter for product class of the device.
     *
     * @param productClass product class of the device.
     */
    public void setProductClass(String productClass);
}
