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

import com.ericsson.commonutil.serialization.Format;
import com.ericsson.commonutil.serialization.SerializationUtil;
import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.genericdevice.GDAccessPermission.Type;
import com.ericsson.deviceaccess.api.genericdevice.GDEventListener;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDService;
import com.ericsson.deviceaccess.spi.GenericDevice;
import static com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity.checkPermission;
import static com.ericsson.deviceaccess.spi.genericdevice.GDActivator.getEventManager;
import com.ericsson.deviceaccess.spi.impl.genericdevice.GDServiceImpl;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.util.HashMap;
import java.util.Map;

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
        checkPermission(GenericDevice.class, Type.GET);
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
    private transient boolean isReady = false;

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
        checkPermission(GenericDevice.class, Type.GET);
        return service.get(name);
    }

    public GDServiceImpl getServiceImpl(String svcName) {
        checkPermission(GenericDevice.class, Type.GET);
        return (GDServiceImpl) service.get(name);
    }

    /**
     * Returns a Map of all the services that the device instance has.
     *
     * @return Map of service instances with their names as key.
     */
    @Override
    public Map<String, GDService> getServices() {
        checkPermission(GenericDevice.class, Type.GET);
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
        checkPermission(GenericDevice.class, Type.SET);
        service.put(svc.getName(), svc);
        svc.updatePath(getPath(true));
        ((com.ericsson.deviceaccess.spi.genericdevice.GDService) svc).setParentDevice(this);
        isReady = true;
    }

    @Override
    public void setId(String id) {
        checkPermission(GenericDevice.class, Type.SET);
        this.id = id;
    }

    @Override
    public String getId() {
        checkPermission(GenericDevice.class, Type.GET);
        return id;
    }

    @Override
    public String getURN() {
        checkPermission(GenericDevice.class, Type.SET);
        return urn;
    }

    @Override
    public void setURN(String urn) {
        checkPermission(GenericDevice.class, Type.SET);
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
        checkPermission(GenericDevice.class, Type.SET);
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
        checkPermission(GenericDevice.class, Type.GET);
        return name;
    }

    @Override
    public void setType(String type) {
        checkPermission(GenericDevice.class, Type.SET);
        this.type = type;
    }

    @Override
    public String getType() {
        checkPermission(GenericDevice.class, Type.GET);
        return type;
    }

    @Override
    public void setProtocol(String protocol) {
        checkPermission(GenericDevice.class, Type.SET);
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        checkPermission(GenericDevice.class, Type.GET);
        return protocol;
    }

    @Override
    public void setLocation(String location) {
        checkPermission(GenericDevice.class, Type.SET);
        this.location = location;
    }

    @Override
    public String getLocation() {
        checkPermission(GenericDevice.class, Type.GET);
        return location;
    }

    @Override
    public void setOnline(boolean online) {
        checkPermission(GenericDevice.class, Type.SET);
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
        checkPermission(GenericDevice.class, Type.GET);
        return online;
    }

    @Override
    public void setIcon(String icon) {
        checkPermission(GenericDevice.class, Type.SET);
        this.icon = icon;
    }

    @Override
    public String getIcon() {
        checkPermission(GenericDevice.class, Type.GET);
        return icon;
    }

    @Override
    public State getState() {
        checkPermission(GenericDevice.class, Type.GET);
        return state;
    }

    public void setState(State state) {
        checkPermission(GenericDevice.class, Type.SET);
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
        checkPermission(GenericDevice.class, Type.GET);
        return path + "/" + this.getId();
    }

    @Override
    public String getPath() {
        checkPermission(GenericDevice.class, Type.GET);
        return path + "/" + this.getId();
    }

    @Override
    public void updatePath(String path) {
        checkPermission(GenericDevice.class, Type.SET);
        this.path = path;
        service.forEach((k, svc) -> svc.updatePath(getPath(true)));
    }

    @Override
    public void setContact(String contact) {
        checkPermission(GenericDevice.class, Type.SET);
        this.contact = contact;
    }

    @Override
    public String getContact() {
        checkPermission(GenericDevice.class, Type.GET);
        return contact;
    }

    /**
     * Sets the list of services in the device instance. Returns a HashMap of
     * all the services that the device has.
     *
     * @param service map of service instances with their names as key.
     */
    public void setService(Map service) {
        checkPermission(GenericDevice.class, Type.SET);
        if (service == null) {
            throw new NullPointerException("Map cannot be null");
        }
        this.service = service;
    }

    @Override
    public void setManufacturer(String manufacturer) {
        checkPermission(GenericDevice.class, Type.SET);
        this.manufacturer = manufacturer;

    }

    @Override
    public String getManufacturer() {
        checkPermission(GenericDevice.class, Type.GET);
        return manufacturer;
    }

    @Override
    public void setModelName(String modelName) {
        checkPermission(GenericDevice.class, Type.SET);
        this.modelName = modelName;
    }

    @Override
    public String getModelName() {
        checkPermission(GenericDevice.class, Type.GET);
        return modelName;
    }

    @Override
    public void setDescription(String description) {
        checkPermission(GenericDevice.class, Type.SET);
        this.description = description;
    }

    @Override
    public String getDescription() {
        checkPermission(GenericDevice.class, Type.GET);
        return description;
    }

    @Override
    public void setSerialNumber(String serialNumber) {
        checkPermission(GenericDevice.class, Type.SET);
        this.serialNumber = serialNumber;
    }

    @Override
    public String getSerialNumber() {
        checkPermission(GenericDevice.class, Type.GET);
        return serialNumber;
    }

    @Override
    public void setProductClass(String productClass) {
        checkPermission(GenericDevice.class, Type.SET);
        this.productClass = productClass;
    }

    @Override
    public String getProductClass() {
        checkPermission(GenericDevice.class, Type.GET);
        return productClass;
    }

    @Override
    public String serialize(Format format) throws GDException {
        checkPermission(GenericDevice.class, Type.GET);
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
        checkPermission(GenericDevice.class, Type.GET);
        try {
            return SerializationUtil.serializeAccordingPath(format, path, Constants.PATH_DELIMITER, this);
        } catch (SerializationUtil.SerializationException ex) {
            throw new GDException(404, ex.getMessage(), ex);
        }
    }

    /**
     * A utility method to get device property dictionary which contains
     * parameters such as device.id, device.type and so on. The dictionary
     * object is meant to be used as OSGi service properties.
     *
     * @return
     */
    @JsonIgnore
    public Map<String, Object> getDeviceProperties() {
        return getDeviceProperties(this);
    }

    public void notifyEvent(String serviceId, final Map<String, Object> parameters) {
        getEventManager().addPropertyEvent(id, serviceId, parameters);
    }

    public void notifyEventRemoved(String serviceId, String propertyId) {
        getEventManager().addStateEvent(id, serviceId, propertyId, GDEventListener.Type.REMOVED);
    }

    public void notifyEventAdded(String serviceId, String propertyId) {
        getEventManager().addStateEvent(id, serviceId, propertyId, GDEventListener.Type.ADDED);
    }

    private String toJsonString(Format format, int indent, boolean stateOnly) throws GDException {
        try {
            return SerializationUtil.get(format).writeValueAsString(this);
        } catch (JsonProcessingException ex) {
            throw new GDException(ex.getMessage(), ex);
        }
    }

}
