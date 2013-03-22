package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.*;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;
import com.ericsson.deviceaccess.spi.GenericDeviceService;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.deviceaccess.spi.utility.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

public class GenericDeviceServiceImpl extends GenericDeviceService.Stub
        implements GenericDeviceService {
    private static MetadataUtil metadataUtil = MetadataUtil.getInstance();
    private String name;
    private String path;
    private GenericDeviceImpl parentDevice;
    private HashMap action = new HashMap();
    private GenericDevicePropertiesImpl properties;
    private GenericDevicePropertyMetadata[] propertyMetadata;

    protected GenericDeviceServiceImpl(String name,
                                       GenericDevicePropertyMetadata[] propertyMetadata) {
        this.propertyMetadata = new GenericDevicePropertyMetadata[propertyMetadata.length + 1];
        System.arraycopy(propertyMetadata, 0, this.propertyMetadata, 0, propertyMetadata.length);
        this.propertyMetadata[this.propertyMetadata.length - 1] =
            new ParameterSchema.Builder(GenericDevicePropertiesImpl.LAST_UPDATE_TIME).setType(Long.class).build();
        properties = new GenericDevicePropertiesImpl(this.propertyMetadata, this);
        this.name = name;
    }

    public void setParentDevice(GenericDevice parentDevice) {
        this.parentDevice = (GenericDeviceImpl) parentDevice;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getActionNames() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());

        return (String[]) action.keySet().toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    public GenericDeviceAction getAction(String name) {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());

        return (GenericDeviceAction) action.get(name);
    }

    public void putAction(GenericDeviceAction act) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        action.put(act.getName(), act);
        act.updatePath(getPath(true));
    }

    protected void addDynamicProperty(GenericDevicePropertyMetadata propertyMetadata) {
        properties.addDynamicProperty(propertyMetadata);
    }

    protected void removeDynamicProperty(GenericDevicePropertyMetadata propertyMetadata) {
        properties.removeDynamicProperty(propertyMetadata);
    }

    protected void notifyEvent(Properties parameters) {
        parentDevice.notifyEvent(getName(), parameters);
    }

	public void notifyEventRemoved(String name) {
		if (parentDevice != null)
			parentDevice.notifyEventRemoved(getName(), name);
	}

	public void notifyEventAdded(String name) {
		if (parentDevice != null)
			parentDevice.notifyEventAdded(getName(), name);
	}

	/**
     * {@inheritDoc}
     */
    public String getName() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public String getPath(boolean isAbsolute) {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return path + "/service/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getPath() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return path + "/service/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    public void updatePath(String path) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.path = path;
        for (Iterator i = action.values().iterator(); i.hasNext(); ) {
            GenericDeviceAction act = (GenericDeviceAction) i.next();
            act.updatePath(getPath(true));
        }

        for (int i = 0; i < propertyMetadata.length; i++) {
            GenericDevicePropertyMetadata md = propertyMetadata[i];
            md.updatePath(getPath(true));
        }
    }

    public final boolean hasProperty(String name) {
        return properties.hasProperty(name);
    }

    /**
     * {@inheritDoc}
     */
    public final GenericDeviceProperties getProperties() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    public GenericDevicePropertyMetadata[] getPropertiesMetadata() {
        return propertyMetadata;
    }

    /**
     * {@inheritDoc}
     */
    public String serialize(int format) throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (format == Serializable.FORMAT_JSON
                || format == Serializable.FORMAT_JSON_WDC) {
            int indent = 0;
            return toJsonString(format, indent);
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }
    }

    public String serializeState() {
        return properties.serializeState();
    }

    /**
     * {@inheritDoc}
     */
    public String getSerializedNode(String path, int format)
            throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (path == null)
            throw new GenericDeviceException(405, "Path cannot be null");

        if (path.length() == 0) {
            return serialize(format);
        } else if (path.equals("name")) {
            return getName();
        } else if (path.startsWith("action")) {
            if (path.indexOf(Constants.PATH_DELIMITER) > 0) {
                path = path
                        .substring(path.indexOf(Constants.PATH_DELIMITER) + 1);
                String actName;
                if (path.indexOf(Constants.PATH_DELIMITER) > 0) {
                    actName = path.substring(0,
                            path.indexOf(Constants.PATH_DELIMITER));
                    path = path.substring(path
                            .indexOf(Constants.PATH_DELIMITER) + 1);
                } else {
                    actName = path;
                    path = "";
                }
                GenericDeviceAction act = (GenericDeviceAction) action
                        .get(actName);
                if (act != null) {
                    return act.getSerializedNode(path, format);
                } else {
                    throw new GenericDeviceException(404, "No such node found");
                }
            } else {
                return serializeActionList(format);
            }
        } else if (path.startsWith("parameter") && propertyMetadata.length > 0) {
            if (path.indexOf(Constants.PATH_DELIMITER) > 0) {
                path = path
                        .substring(path.indexOf(Constants.PATH_DELIMITER) + 1);
                if (path.length() > 0) {
                    return properties.getStringValue(path.substring(path
                            .indexOf(Constants.PATH_DELIMITER) + 1));
                } else {
                    return properties.serialize(format);
                }
            } else {
                return properties.serialize(format);
            }
        } else {
            throw new GenericDeviceException(404, "No such node found");
        }
    }

    private String serializeActionList(int format)
            throws GenericDeviceException {
        if (format == Serializable.FORMAT_JSON
                || format == Serializable.FORMAT_JSON_WDC) {
            return getActionListJsonString(format, 0);
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }
    }

    private String toJsonString(int format, int indent)
            throws GenericDeviceException {
        String json = "{";
        json += "\"name\":\"" + Utils.escapeJSON(getName()) + "\",";
        json += "\"actions\":" + getActionListJsonString(format, indent) + ",";
        json += "\"properties\": " + properties.serialize(format);
        json += "}";
        return json;
    }

    private String getActionListJsonString(int format, int indent)
            throws GenericDeviceException {
        String json = "{";
        Iterator it = action.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            GenericDeviceAction act = (GenericDeviceAction) action.get(key);
            if (act != null) {
                json += '"' + Utils.escapeJSON(act.getName()) +"\":" + act.serialize(format);
                if (it.hasNext())
                    json += ",";
            }
        }
        json += "}";
        return json;
    }

    public GenericDevice getParentDevice() {
        return parentDevice;
    }
}
