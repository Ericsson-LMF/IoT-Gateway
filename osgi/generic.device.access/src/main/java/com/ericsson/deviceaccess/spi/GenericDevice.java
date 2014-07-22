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
package com.ericsson.deviceaccess.spi;

import com.ericsson.deviceaccess.api.genericdevice.GDService;

/**
 * This is the interface of a generic device to be used by service providers.
 */
public interface GenericDevice extends com.ericsson.deviceaccess.api.GenericDevice {

    String ID = "id";
    String URN = "urn";
    String NAME = "name";
    String TYPE = "type";
    String PROTOCOL = "protocol";
    String LOCATION = "location";
    String ONLINE = "online";
    String ICON = "icon";
    String PATH = "path";
    String CONTACT = "contact";
    String MANUFACTURER = "manufacturer";
    String MODEL_NAME = "modelName";
    String DESCRIPTION = "description";
    String SERIAL_NUMBER = "serialNumber";
    String PRODUCT_CLASS = "productClass";

    /**
     * Method to put an instance that implements GenericDeviceService into the
     * device object.
     *
     * @param svc An instance that implements the service to be put.
     */
    void putService(GDService svc);

    /**
     * Setter for the device ID field. Non-null value must be set.
     *
     * @param id Device ID.
     */
    void setId(String id);

    /**
     * Setter for the friendly name of the device.
     *
     * @param name Friendly name of the device.
     */
    @Override
     void setName(String name);

    /**
     * Setter for the device type field.
     *
     * @param type Device type
     */
    void setType(String type);

    /**
     * Setter for the name of protocol used for discovery of the device. The
     * value should be chosen from the constant properties defined in
     * com.ericsson.deviceaccess.api.Constants that start with prefix "PROTO_".
     *
     * @param protocol Name of the protocol used for discovery of the device.
     */
    void setProtocol(String protocol);

    /**
     * Setter for location where the device is discovered.
     *
     * @param location Location where the device is discovered.
     */
    void setLocation(String location);

    /**
     * Setter for the boolean field that indicates if the device is online, i.e.
     * can be communicated through the GenericDevice API or not.
     *
     * @param online true if the device is online. false otherwise.
     */
    void setOnline(boolean online);

    /**
     * Setter for the icon url field. The value should be URL for an icon image
     * of the device that is available for the clients to fetch, i.e. image file
     * on an HTTP server.
     *
     * @param icon URL for an icon image of the device.
     */
    void setIcon(String icon);

    /**
     * @param contact Contact URL of the device.
     * @deprecate Setter for contact URL of the device that is used in Web
     * Device Connectivity. This method should be removed because each
     * application should have its own way of exposing the device and thus the
     * contact URL of the device depends.
     */
    void setContact(String contact);

    /**
     * Setter for name of the manufacturer of the device.
     *
     * @param manufacturer name of manufacturer of the device.
     */
    void setManufacturer(String manufacturer);

    /**
     * Setter for model name of the device.
     *
     * @param modelName model name of the device.
     */
    void setModelName(String modelName);

    /**
     * Setter for the description of the device.
     *
     * @param description description of the device.
     */
    void setDescription(String description);

    /**
     * Setter for the serial number of the device.
     *
     * @param serialNumber Serial number of the device.
     */
    void setSerialNumber(String serialNumber);

    /**
     * Setter for product class of the device.
     *
     * @param productClass product class of the device.
     */
    void setProductClass(String productClass);
}
