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
package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.*;
import com.ericsson.deviceaccess.spi.GenericDevice;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;
import com.ericsson.deviceaccess.spi.GenericDeviceActivator;
import com.ericsson.deviceaccess.spi.utility.Utils;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

public abstract class GenericDeviceImpl extends GenericDevice.Stub implements GenericDevice {

    private String id = "undefined";
    private String urn = "undefined";
    private String name = "undefined";
    private String type = "undefined";
    private String protocol = "undefined";
    private String location = "undefined";
    private boolean online;
    private String icon = "undefined";
    private String path = "undefined";
    private String contact = "undefined";
    private String manufacturer = "undefined";
    private String modelName = "undefined";
    private String description = "undefined";
    private String serialNumber = "undefined";
    private String productClass = "undefined";
    private String state = STATE_READY;

    //private HashMap<String,GenericDeviceServiceImpl> service = new HashMap<String,GenericDeviceServiceImpl>();
    private Map service = new HashMap();
    private boolean isReady = false;

    /**
     *
     */
    protected GenericDeviceImpl() {
    }

    /**
     * Returns a service instance if the device has a service with the specified
     * name or null, otherwise.
     *
     * @param name The name of service that is in question.
     * @return device instance or null
     */
    public GenericDeviceService getService(String name) {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return (GenericDeviceService) service.get(name);
    }

    public GenericDeviceServiceImpl getServiceImpl(String svcName) {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return (GenericDeviceServiceImpl) service.get(name);
    }

    /**
     * Returns a Map of all the services that the device instance has.
     *
     * @return Map of service instances with their names as key.
     */
    public Map getService() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return new HashMap(service);
    }

    /**
     * Puts an instance of a service. If an instance which has the same name is
     * already registered, the instance is updated.
     *
     * @param svc A service instance which is being put.
     */
    public void putService(GenericDeviceService svc) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        service.put(svc.getName(), svc);
        svc.updatePath(getPath(true));
        ((com.ericsson.deviceaccess.spi.GenericDeviceService) svc).setParentDevice(this);
        isReady = true;
    }

    /**
     * @param name
     * @param svc
     * @deprecate
     */
    public void putService(String name, GenericDeviceService svc) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        service.put(name, svc);
        svc.updatePath(getPath(true));
        ((com.ericsson.deviceaccess.spi.GenericDeviceService) svc).setParentDevice(this);
        isReady = true;
    }

    public void setId(String id) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.id = id;
    }

    public String getId() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return id;
    }

    public String getURN() {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        return urn;
    }

    public void setURN(String urn) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        if (urn == null) {
            throw new IllegalArgumentException("URN may not be null");
        }
        String oldUrn = this.urn;
        this.urn = urn;
        if (isReady && urn != null && !urn.equals(oldUrn)) {
            notifyEvent("DeviceProperties", new Properties() {
                {
                    put(GenericDeviceEventListener.DEVICE_URN, new String(GenericDeviceImpl.this.urn));
                }
            });
        }
    }

    public void setName(String name) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        String oldName = this.name;
        this.name = name;
        if (isReady && name != null && !name.equals(oldName)) {
            notifyEvent("DeviceProperties", new Properties() {
                {
                    put(GenericDeviceEventListener.DEVICE_NAME, new String(GenericDeviceImpl.this.name));
                }
            });
        }
    }

    public String getName() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return name;
    }

    public void setType(String type) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.type = type;
    }

    public String getType() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return type;
    }

    public void setProtocol(String protocol) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.protocol = protocol;
    }

    public String getProtocol() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return protocol;
    }

    public void setLocation(String location) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.location = location;
    }

    public String getLocation() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return location;
    }

    public void setOnline(boolean online) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        boolean oldOnline = this.online;
        this.online = online;
        if (isReady && online != oldOnline) {
            notifyEvent("DeviceProperties", new Properties() {
                {
                    put(GenericDeviceEventListener.DEVICE_ONLINE, new Boolean(GenericDeviceImpl.this.online));
                }
            });
        }
    }

    public boolean isOnline() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return online;
    }

    public void setIcon(String icon) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.icon = icon;
    }

    public String getIcon() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return icon;
    }

    public String getState() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return state;
    }

    public void setState(String state) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        String oldState = this.state;
        this.state = state;
        if (isReady && ((state == null && oldState != null) || (state != null && !state.equals(oldState)))) {
            notifyEvent("DeviceProperties", new Properties() {
                {
                    put(GenericDeviceEventListener.DEVICE_STATE, new String(GenericDeviceImpl.this.state));
                }
            });
        }
    }

    public String getPath(boolean isAbsolute) {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return path + "/" + this.getId();
    }

    public String getPath() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return path + "/" + this.getId();
    }

    public void updatePath(String path) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.path = path;
        for (Iterator i = service.values().iterator(); i.hasNext();) {
            GenericDeviceService svc = (GenericDeviceService) i.next();
            svc.updatePath(getPath(true));
        }
    }

    public void setContact(String contact) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.contact = contact;
    }

    public String getContact() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return contact;
    }

    /**
     * Sets the list of services in the device instance. Returns a HashMap of
     * all the services that the device has.
     *
     * @return HashMap of service instances with their names as key.
     */
    public void setService(Map service) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.service = service;
    }

    public void setManufacturer(String manufacturer) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.manufacturer = manufacturer;

    }

    public String getManufacturer() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());

        return manufacturer;
    }

    public void setModelName(String modelName) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.modelName = modelName;
    }

    public String getModelName() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());

        return modelName;
    }

    public void setDescription(String description) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.description = description;
    }

    public String getDescription() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());

        return description;
    }

    public void setSerialNumber(String serialNumber) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.serialNumber = serialNumber;
    }

    public String getSerialNumber() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return serialNumber;
    }

    public void setProductClass(String productClass) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.productClass = productClass;
    }

    public String getProductClass() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return productClass;
    }

    public String serialize(int format) throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());

        if (format == Serializable.FORMAT_JSON || format == Serializable.FORMAT_JSON_WDC) {
            int indent = 0;
            return toJsonString(format, indent, false);
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }
    }

    public String serializeState() throws GenericDeviceException {
        return toJsonString(Serializable.FORMAT_JSON, 0, true);
    }

    public String getSerializedNode(String path, int format) throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (path == null) {
            throw new GenericDeviceException(405, "Path cannot be null");
        }
        if (path.length() == 0) {
            return serialize(format);
        } else if (path.startsWith("service") && service != null) {
            if (path.indexOf(Constants.PATH_DELIMITER) > 0) {
                path = path.substring(path.indexOf(Constants.PATH_DELIMITER) + 1);
                String svcName;
                if (path.indexOf(Constants.PATH_DELIMITER) > 0) {
                    svcName = path.substring(0, path.indexOf(Constants.PATH_DELIMITER));
                    path = path.substring(path.indexOf(Constants.PATH_DELIMITER) + 1);
                } else {
                    svcName = path;
                    path = "";
                }
                GenericDeviceService svc = (GenericDeviceService) service.get(svcName);
                if (svc != null) {
                    return svc.getSerializedNode(path, format);
                } else {
                    throw new GenericDeviceException(404, "No such node found");
                }
            } else {
                return serializeServiceList(format);
            }
        } else if (path.indexOf(Constants.PATH_DELIMITER) < 0) {
            return (String) getFieldValue(path);
        } else {
            throw new GenericDeviceException(404, "No such node found");
        }
    }

    /**
     * A utility method to get device property dictionary which contains
     * parameters such as device.id, device.type and so on. The dictionary
     * object is meant to be used as OSGi service properties.
     *
     * @return
     */
    public Properties getDeviceProperties() {
        return getDeviceProperties(this);
    }

    /**
     * Static utility method which generates a Properties object from
     * GenericDevice object. It is for making it easy to register the instance
     * to OSGi framework as a service.
     *
     * @return Properties object that can be used in registerService() method.
     */
    public static Properties getDeviceProperties(com.ericsson.deviceaccess.api.GenericDevice dev) {
        GenericDeviceAccessSecurity.checkGetPermission(GenericDevice.class.getName());
        Properties props = new Properties();
        if (dev.getURN() != null) {
            props.setProperty(Constants.PARAM_DEVICE_URN, dev.getURN());
        }
        if (dev.getId() != null) {
            props.setProperty(Constants.PARAM_DEVICE_ID, dev.getId());
        }
        if (dev.getType() != null) {
            props.setProperty(Constants.PARAM_DEVICE_TYPE, dev.getType());
        }
        if (dev.getProtocol() != null) {
            props.setProperty(Constants.PARAM_DEVICE_PROTOCOL, dev.getProtocol());
        }
        return props;
    }

    public void notifyEvent(String serviceId, final Properties parameters) {
        GenericDeviceActivator.getEventManager().addEvent(id, serviceId, parameters);
    }

    public void notifyEventRemoved(String serviceId, String propertyId) {
        GenericDeviceActivator.getEventManager().addRemoveEvent(id, serviceId, propertyId);
    }

    public void notifyEventAdded(String serviceId, String propertyId) {
        GenericDeviceActivator.getEventManager().addAddEvent(id, serviceId, propertyId);
    }

    private String serializeServiceList(int format) throws GenericDeviceException {
        if (format == Serializable.FORMAT_JSON || format == Serializable.FORMAT_JSON_WDC) {
            int indent = 0;
            if (format == Serializable.FORMAT_JSON_WDC) {
                indent = 3;
            }
            return getServiceListJsonString(format, indent, false);
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }
    }

    private String toJsonString(int format, int indent, boolean stateOnly) throws GenericDeviceException {
        String json = "{";
        json += "\"id\":\"" + getId() + "\"";
        json += ",";
        String[] fields = {"URN", "name", "type", "icon", "protocol", "location", "manufacturer", "contact", "description", "modelName", "productClass", "serialNumber", "state"};
        for (int i = 0; i < fields.length; i++) {
            Object value = getFieldValue(fields[i]);
            if (value != null) {
                json += "\"" + fields[i] + "\":";
                json += "\"" + Utils.escapeJSON(value.toString()) + "\"";
                if (i < fields.length - 1) {
                    json += ",";
                }
            }
        }
        json += ",";
        json += "\"online\":" + isOnline();
        json += ",";
        json += "\"services\": {" + getServiceListJsonString(format, indent, stateOnly) + "}";
        json += "}";
        return json;
    }

    private String getServiceListJsonString(int format, int indent, boolean stateOnly) throws GenericDeviceException {
        String json = "";
        Iterator it = service.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            GenericDeviceService svc = (GenericDeviceService) service.get(key);
            json += "\"" + Utils.escapeJSON(svc.getName()) + "\":";
            json += (stateOnly ? svc.serializeState() : svc.serialize(format));
            if (it.hasNext()) {
                json += ",";
            }
        }
        json += "";
        return json;

    }

    private Object getFieldValue(String name) throws GenericDeviceException {
        Method method;
        try {
            method = getClass().getMethod("get" + makeFirstCharToUpperCase(name), null);
            if (method != null) {
                return method.invoke(this, null);
            }
        } catch (Exception e) {
            throw new GenericDeviceException("Exception when getting field value", e);
        }
        return null;
    }

    private String makeFirstCharToUpperCase(String value) {
        if (value != null && value.length() > 0) {
            return value.substring(0, 1).toUpperCase() + value.substring(1);
        }
        return "";
    }

    public String[] getServiceNames() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return (String[]) service.keySet().toArray(new String[0]);
    }

}
