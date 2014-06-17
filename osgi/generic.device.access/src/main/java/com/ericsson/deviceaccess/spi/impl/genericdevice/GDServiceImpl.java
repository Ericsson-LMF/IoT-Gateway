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
package com.ericsson.deviceaccess.spi.impl.genericdevice;

import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.genericdevice.GDAction;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import static com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity.checkGetPermission;
import static com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity.checkSetPermission;
import com.ericsson.deviceaccess.spi.genericdevice.GDService;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceImpl;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.ericsson.research.commonutil.StringUtil;
import com.ericsson.research.commonutil.function.FunctionalUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class GDServiceImpl extends GDService.Stub
        implements GDService {

    private final String name;
    private String path;
    private GenericDeviceImpl parentDevice;
    private final Map<String, GDAction> action = new HashMap<>();
    private final GDPropertiesImpl properties;
    private final List<GDPropertyMetadata> propertyMetadata;

    public GDServiceImpl(String name,
            List<GDPropertyMetadata> propertyMetadata) {
        this.propertyMetadata = new ArrayList(propertyMetadata);
        this.propertyMetadata.add(new ParameterSchema.Builder(GDPropertiesImpl.LAST_UPDATE_TIME, Long.class).build());
        properties = new GDPropertiesImpl(this.propertyMetadata, this);
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
    public GDAction getAction(String name) {
        checkGetPermission(getClass().getName());
        return action.get(name);
    }

    public void putAction(GDAction act) {
        checkSetPermission(getClass().getName());
        action.put(act.getName(), act);
        act.updatePath(getPath(true));
    }

    protected void addDynamicProperty(GDPropertyMetadata propertyMetadata) {
        properties.addDynamicProperty(propertyMetadata);
    }

    protected void removeDynamicProperty(GDPropertyMetadata propertyMetadata) {
        properties.removeDynamicProperty(propertyMetadata);
    }

    protected void notifyEvent(HashMap<String, Object> parameters) {
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

        for (GDPropertyMetadata md : propertyMetadata) {
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
    public final GDProperties getProperties() {
        checkGetPermission(getClass().getName());
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GDPropertyMetadata[] getPropertiesMetadata() {
        return propertyMetadata.toArray(new GDPropertyMetadata[propertyMetadata.size()]);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize(Format format) throws GDException {
        checkGetPermission(getClass().getName());
        if (format.isJson()) {
            int indent = 0;
            return toJsonString(format, indent);
        } else {
            throw new GDException(405, "No such format supported");
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
            throws GDException {
        checkGetPermission(getClass().getName());
        if (path == null) {
            throw new GDException(405, "Path cannot be null");
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
                GDAction act = action
                        .get(actName);
                if (act != null) {
                    return act.getSerializedNode(path, format);
                } else {
                    throw new GDException(404, "No such node found");
                }
            } else {
                return serializeActionList(format);
            }
        } else if (path.startsWith("parameter") && !propertyMetadata.isEmpty()) {
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
            throw new GDException(404, "No such node found");
        }
    }

    private String serializeActionList(Format format)
            throws GDException {
        if (format.isJson()) {
            return getActionListJsonString(format, 0);
        } else {
            throw new GDException(405, "No such format supported");
        }
    }

    private String toJsonString(Format format, int indent)
            throws GDException {
        String json = "{";
        json += "\"name\":\"" + StringUtil.escapeJSON(getName()) + "\",";
        json += "\"actions\":" + getActionListJsonString(format, indent) + ",";
        json += "\"properties\": " + properties.serialize(format);
        json += "}";
        return json;
    }

    private String getActionListJsonString(Format format, int indent)
            throws GDException {
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
            throw (GDException) ex.getCause();
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
