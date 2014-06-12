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

import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.GenericDeviceAction;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.api.GenericDevicePropertyMetadata;
import static com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity.checkGetPermission;
import static com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity.checkSetPermission;
import com.ericsson.deviceaccess.spi.GenericDeviceService;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.research.commonutil.StringUtil;
import com.ericsson.research.commonutil.function.FunctionalUtil;
import java.util.HashMap;
import java.util.Properties;

public class GenericDeviceServiceImpl extends GenericDeviceService.Stub
        implements GenericDeviceService {

    private String name;
    private String path;
    private GenericDeviceImpl parentDevice;
    private HashMap<String, GenericDeviceAction> action = new HashMap();
    private GenericDevicePropertiesImpl properties;
    private GenericDevicePropertyMetadata[] propertyMetadata;

    protected GenericDeviceServiceImpl(String name,
            GenericDevicePropertyMetadata[] propertyMetadata) {
        this.propertyMetadata = new GenericDevicePropertyMetadata[propertyMetadata.length + 1];
        System.arraycopy(propertyMetadata, 0, this.propertyMetadata, 0, propertyMetadata.length);
        this.propertyMetadata[this.propertyMetadata.length - 1]
                = new ParameterSchema.Builder().setName(GenericDevicePropertiesImpl.LAST_UPDATE_TIME).setType(Long.class).build();
        properties = new GenericDevicePropertiesImpl(this.propertyMetadata, this);
        this.name = name;
    }

    @Override
    public void setParentDevice(GenericDevice parentDevice) {
        this.parentDevice = (GenericDeviceImpl) parentDevice;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getActionNames() {
        checkGetPermission(getClass().getName());
        return action.keySet().toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericDeviceAction getAction(String name) {
        checkGetPermission(getClass().getName());
        return action.get(name);
    }

    public void putAction(GenericDeviceAction act) {
        checkSetPermission(getClass().getName());
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
        if (parentDevice != null) {
            parentDevice.notifyEventRemoved(getName(), name);
        }
    }

    public void notifyEventAdded(String name) {
        if (parentDevice != null) {
            parentDevice.notifyEventAdded(getName(), name);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        checkGetPermission(getClass().getName());
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath(boolean isAbsolute) {
        checkGetPermission(getClass().getName());
        return path + "/service/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        checkGetPermission(getClass().getName());
        return path + "/service/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePath(String path) {
        checkSetPermission(getClass().getName());
        this.path = path;
        action.forEach((k, act) -> act.updatePath(getPath(true)));

        for (GenericDevicePropertyMetadata md : propertyMetadata) {
            md.updatePath(getPath(true));
        }
    }

    public final boolean hasProperty(String name) {
        return properties.hasProperty(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final GenericDeviceProperties getProperties() {
        checkGetPermission(getClass().getName());
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GenericDevicePropertyMetadata[] getPropertiesMetadata() {
        return propertyMetadata;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize(Format format) throws GenericDeviceException {
        checkGetPermission(getClass().getName());
        if (format.isJson()) {
            int indent = 0;
            return toJsonString(format, indent);
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }
    }

    @Override
    public String serializeState() {
        return properties.serializeState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSerializedNode(String path, Format format)
            throws GenericDeviceException {
        checkGetPermission(getClass().getName());
        if (path == null) {
            throw new GenericDeviceException(405, "Path cannot be null");
        }

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
                GenericDeviceAction act = action
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

    private String serializeActionList(Format format)
            throws GenericDeviceException {
        if (format.isJson()) {
            return getActionListJsonString(format, 0);
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }
    }

    private String toJsonString(Format format, int indent)
            throws GenericDeviceException {
        String json = "{";
        json += "\"name\":\"" + StringUtil.escapeJSON(getName()) + "\",";
        json += "\"actions\":" + getActionListJsonString(format, indent) + ",";
        json += "\"properties\": " + properties.serialize(format);
        json += "}";
        return json;
    }

    private String getActionListJsonString(Format format, int indent)
            throws GenericDeviceException {
        StringBuilder json = new StringBuilder("{");
        try {
            action.forEach((k, act) -> {
                if (act != null) {
                    json.append('"').append(StringUtil.escapeJSON(act.getName())).append('"').append(':');
                    json.append(FunctionalUtil.smuggle(() -> act.serialize(format)));
                    json.append(',');
                }
            });
        } catch (RuntimeException ex) {
            throw (GenericDeviceException) ex.getCause();
        }
        if (json.length() > 0) {
            json.setLength(json.length() - 1);
        }
        return json.append("}").toString();
    }

    public GenericDevice getParentDevice() {
        return parentDevice;
    }
}
