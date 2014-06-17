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

import com.ericsson.deviceaccess.spi.impl.genericdevice.GDServiceImpl;
import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.genericdevice.GDEventListener;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDService;
import com.ericsson.deviceaccess.spi.GenericDevice;
import static com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity.checkGetPermission;
import static com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity.checkSetPermission;
import com.ericsson.deviceaccess.spi.genericdevice.GDActivator;
import com.ericsson.research.commonutil.StringUtil;
import com.ericsson.research.commonutil.function.FunctionalUtil;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public abstract class GenericDeviceImpl extends GenericDevice.Stub implements GenericDevice {

    /**
     * Static utility method which generates a Properties object from
     * GenericDevice object. It is for making it easy to register the instance
     * to OSGi framework as a service.
     *
     * @param dev device to generate properties from
     * @return Properties object that can be used in registerService() method.
     */
    public static Map<String, Object> getDeviceProperties(com.ericsson.deviceaccess.api.GenericDevice dev) {
        checkGetPermission(GenericDevice.class.getName());
        Map<String, Object> props = new HashMap<>();
        if (dev.getURN() != null) {
            props.put(Constants.PARAM_DEVICE_URN, dev.getURN());
        }
        if (dev.getId() != null) {
            props.put(Constants.PARAM_DEVICE_ID, dev.getId());
        }
        if (dev.getType() != null) {
            props.put(Constants.PARAM_DEVICE_TYPE, dev.getType());
        }
        if (dev.getProtocol() != null) {
            props.put(Constants.PARAM_DEVICE_PROTOCOL, dev.getProtocol());
        }
        return props;
    }

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
    private State state = State.READY;

    private Map<String, GDService> service = new HashMap<>();
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
    @Override
    public GDService getService(String name) {
        checkGetPermission(getClass().getName());
        return service.get(name);
    }

    public GDServiceImpl getServiceImpl(String svcName) {
        checkGetPermission(getClass().getName());
        return (GDServiceImpl) service.get(name);
    }

    /**
     * Returns a Map of all the services that the device instance has.
     *
     * @return Map of service instances with their names as key.
     */
    public Map<String, GDService> getService() {
        checkGetPermission(getClass().getName());
        return new HashMap<>(service);
    }

    /**
     * Puts an instance of a service. If an instance which has the same name is
     * already registered, the instance is updated.
     *
     * @param svc A service instance which is being put.
     */
    @Override
    public void putService(GDService svc) {
        checkSetPermission(getClass().getName());
        service.put(svc.getName(), svc);
        svc.updatePath(getPath(true));
        ((com.ericsson.deviceaccess.spi.genericdevice.GDService) svc).setParentDevice(this);
        isReady = true;
    }

    /**
     * @param name
     * @param svc
     * @deprecate
     */
    public void putService(String name, GDService svc) {
        checkSetPermission(getClass().getName());
        service.put(name, svc);
        svc.updatePath(getPath(true));
        ((com.ericsson.deviceaccess.spi.genericdevice.GDService) svc).setParentDevice(this);
        isReady = true;
    }

    @Override
    public void setId(String id) {
        checkSetPermission(getClass().getName());
        this.id = id;
    }

    @Override
    public String getId() {
        checkGetPermission(getClass().getName());
        return id;
    }

    @Override
    public String getURN() {
        checkSetPermission(getClass().getName());
        return urn;
    }

    @Override
    public void setURN(String urn) {
        checkSetPermission(getClass().getName());
        if (urn == null) {
            throw new IllegalArgumentException("URN may not be null");
        }
        String oldUrn = this.urn;
        this.urn = urn;
        if (isReady && !urn.equals(oldUrn)) {
            notifyEvent("DeviceProperties", new HashMap() {
                {
                    put(GDEventListener.DEVICE_URN, GenericDeviceImpl.this.urn);
                }
            });
        }
    }

    @Override
    public void setName(String name) {
        checkSetPermission(getClass().getName());
        if (name == null) {
            throw new IllegalArgumentException("Name may not be null");
        }
        String oldName = this.name;
        this.name = name;
        if (isReady && !name.equals(oldName)) {
            notifyEvent("DeviceProperties", new HashMap() {
                {
                    put(GDEventListener.DEVICE_NAME, GenericDeviceImpl.this.name);
                }
            });
        }
    }

    @Override
    public String getName() {
        checkGetPermission(getClass().getName());
        return name;
    }

    @Override
    public void setType(String type) {
        checkSetPermission(getClass().getName());
        this.type = type;
    }

    @Override
    public String getType() {
        checkGetPermission(getClass().getName());
        return type;
    }

    @Override
    public void setProtocol(String protocol) {
        checkSetPermission(getClass().getName());
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        checkGetPermission(getClass().getName());
        return protocol;
    }

    @Override
    public void setLocation(String location) {
        checkSetPermission(getClass().getName());
        this.location = location;
    }

    @Override
    public String getLocation() {
        checkGetPermission(getClass().getName());
        return location;
    }

    @Override
    public void setOnline(boolean online) {
        checkSetPermission(getClass().getName());
        boolean oldOnline = this.online;
        this.online = online;
        if (isReady && online != oldOnline) {
            notifyEvent("DeviceProperties", new HashMap() {
                {
                    put(GDEventListener.DEVICE_ONLINE, GenericDeviceImpl.this.online);
                }
            });
        }
    }

    @Override
    public boolean isOnline() {
        checkGetPermission(getClass().getName());
        return online;
    }

    @Override
    public void setIcon(String icon) {
        checkSetPermission(getClass().getName());
        this.icon = icon;
    }

    @Override
    public String getIcon() {
        checkGetPermission(getClass().getName());
        return icon;
    }

    @Override
    public State getState() {
        checkGetPermission(getClass().getName());
        return state;
    }

    public void setState(State state) {
        checkSetPermission(getClass().getName());
        State oldState = this.state;
        this.state = state;
        if (isReady && ((state == null && oldState != null) || (state != null && !state.equals(oldState)))) {
            notifyEvent("DeviceProperties", new HashMap() {
                {
                    put(GDEventListener.DEVICE_STATE, GenericDeviceImpl.this.state);
                }
            });
        }
    }

    @Override
    public String getPath(boolean isAbsolute) {
        checkGetPermission(getClass().getName());
        return path + "/" + this.getId();
    }

    @Override
    public String getPath() {
        checkGetPermission(getClass().getName());
        return path + "/" + this.getId();
    }

    @Override
    public void updatePath(String path) {
        checkSetPermission(getClass().getName());
        this.path = path;
        service.forEach((k, svc) -> svc.updatePath(getPath(true)));
    }

    @Override
    public void setContact(String contact) {
        checkSetPermission(getClass().getName());
        this.contact = contact;
    }

    @Override
    public String getContact() {
        checkGetPermission(getClass().getName());
        return contact;
    }

    /**
     * Sets the list of services in the device instance. Returns a HashMap of
     * all the services that the device has.
     *
     * @param service map of service instances with their names as key.
     */
    public void setService(Map service) {
        checkSetPermission(getClass().getName());
        this.service = service;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        checkSetPermission(getClass().getName());
        this.manufacturer = manufacturer;

    }

    @Override
    public String getManufacturer() {
        checkGetPermission(getClass().getName());
        return manufacturer;
    }

    @Override
    public void setModelName(String modelName) {
        checkSetPermission(getClass().getName());
        this.modelName = modelName;
    }

    @Override
    public String getModelName() {
        checkGetPermission(getClass().getName());
        return modelName;
    }

    @Override
    public void setDescription(String description) {
        checkSetPermission(getClass().getName());
        this.description = description;
    }

    @Override
    public String getDescription() {
        checkGetPermission(getClass().getName());
        return description;
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        checkSetPermission(getClass().getName());
        this.serialNumber = serialNumber;
    }

    @Override
    public String getSerialNumber() {
        checkGetPermission(getClass().getName());
        return serialNumber;
    }

    @Override
    public void setProductClass(String productClass) {
        checkSetPermission(getClass().getName());
        this.productClass = productClass;
    }

    @Override
    public String getProductClass() {
        checkGetPermission(getClass().getName());
        return productClass;
    }

    @Override
    public String serialize(Format format) throws GDException {
        checkGetPermission(getClass().getName());
        if (format.isJson()) {
            int indent = 0;
            return toJsonString(format, indent, false);
        } else {
            throw new GDException(405, "No such format supported");
        }
    }

    @Override
    public String serializeState() throws GDException {
        return toJsonString(Format.JSON, 0, true);
    }

    @Override
    public String getSerializedNode(String path, Format format) throws GDException {
        checkGetPermission(getClass().getName());
        if (path == null) {
            throw new GDException(405, "Path cannot be null");
        }
        if (path.isEmpty()) {
            return serialize(format);
        } else if (path.startsWith("service") && service != null) {
            if (path.contains(Constants.PATH_DELIMITER)) {
                path = path.substring(path.indexOf(Constants.PATH_DELIMITER) + 1);
                String svcName;
                if (path.contains(Constants.PATH_DELIMITER)) {
                    svcName = path.substring(0, path.indexOf(Constants.PATH_DELIMITER));
                    path = path.substring(path.indexOf(Constants.PATH_DELIMITER) + 1);
                } else {
                    svcName = path;
                    path = "";
                }
                GDService svc = service.get(svcName);
                if (svc != null) {
                    return svc.getSerializedNode(path, format);
                } else {
                    throw new GDException(404, "No such node found");
                }
            } else {
                return serializeServiceList(format);
            }
        } else if (!path.contains(Constants.PATH_DELIMITER)) {
            return (String) getFieldValue(path);
        } else {
            throw new GDException(404, "No such node found");
        }
    }

    /**
     * A utility method to get device property dictionary which contains
     * parameters such as device.id, device.type and so on. The dictionary
     * object is meant to be used as OSGi service properties.
     *
     * @return
     */
    public Map<String, Object> getDeviceProperties() {
        return getDeviceProperties(this);
    }

    public void notifyEvent(String serviceId, final Map<String, Object> parameters) {
        GDActivator.getEventManager().addEvent(id, serviceId, parameters);
    }

    public void notifyEventRemoved(String serviceId, String propertyId) {
        GDActivator.getEventManager().addRemoveEvent(id, serviceId, propertyId);
    }

    public void notifyEventAdded(String serviceId, String propertyId) {
        GDActivator.getEventManager().addAddEvent(id, serviceId, propertyId);
    }

    private String serializeServiceList(Format format) throws GDException {
        if (format.isJson()) {
            int indent = 0;
            if (format == Format.JSON_WDC) {
                indent = 3;
            }
            return getServiceListJsonString(format, indent, false);
        } else {
            throw new GDException(405, "No such format supported");
        }
    }

    private String toJsonString(Format format, int indent, boolean stateOnly) throws GDException {
        String json = "{";
        json += "\"id\":\"" + getId() + "\"";
        json += ",";
        String[] fields = {"URN", "name", "type", "icon", "protocol", "location", "manufacturer", "contact", "description", "modelName", "productClass", "serialNumber", "state"};
        for (int i = 0; i < fields.length; i++) {
            Object value = getFieldValue(fields[i]);
            if (value != null) {
                json += "\"" + fields[i] + "\":";
                json += "\"" + StringUtil.escapeJSON(value.toString()) + "\"";
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

    private String getServiceListJsonString(Format format, int indent, boolean stateOnly) throws GDException {
        StringBuilder json = new StringBuilder();
        try {
            service.forEach((k, srv) -> {
                json.append('"').append(StringUtil.escapeJSON(srv.getName())).append('"').append(':');
                if (stateOnly) {
                    json.append(srv.serializeState());
                } else {
                    json.append(FunctionalUtil.smuggle(() -> srv.serialize(format)));
                }
                json.append(",");
            });
        } catch (RuntimeException ex) {
            throw (GDException) ex.getCause();
        }
        if (json.length() > 0) {
            json.setLength(json.length() - 1);
        }
        return json.toString();
    }

    private Object getFieldValue(String name) throws GDException {
        Method method;
        try {
            method = getClass().getMethod("get" + makeFirstCharToUpperCase(name));
            if (method != null) {
                return method.invoke(this);
            }
        } catch (IllegalAccessException | IllegalArgumentException | NoSuchMethodException | SecurityException | InvocationTargetException e) {
            throw new GDException("Exception when getting field value", e);
        }
        return null;
    }

    private String makeFirstCharToUpperCase(String value) {
        if (value != null && value.length() > 0) {
            return value.substring(0, 1).toUpperCase() + value.substring(1);
        }
        return "";
    }

    @Override
    public String[] getServiceNames() {
        checkGetPermission(getClass().getName());
        return service.keySet().toArray(new String[0]);
    }

}
