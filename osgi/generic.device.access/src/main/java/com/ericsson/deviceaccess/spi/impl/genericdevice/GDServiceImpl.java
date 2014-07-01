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

import static com.ericsson.commonutil.PathUtil.andThen;
import static com.ericsson.commonutil.PathUtil.splits;
import static com.ericsson.commonutil.PathUtil.startsWith;
import com.ericsson.commonutil.StringUtil;
import com.ericsson.commonutil.function.FunctionalUtil;
import com.ericsson.commonutil.json.JsonUtil;
import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.api.genericdevice.GDAccessPermission.Type;
import com.ericsson.deviceaccess.api.genericdevice.GDAction;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import static com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity.checkPermission;
import com.ericsson.deviceaccess.spi.genericdevice.GDService;
import com.ericsson.deviceaccess.spi.impl.GenericDeviceImpl;
import com.ericsson.deviceaccess.spi.schema.ParameterSchema;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GDServiceImpl extends GDService.Stub
        implements GDService {

    private final String name;
    private transient String path;
    private transient GenericDeviceImpl parentDevice;
    private final Map<String, GDAction> action = new HashMap<>();
    private final GDPropertiesImpl properties;
    private transient final List<GDPropertyMetadata> propertyMetadata;

    public GDServiceImpl(String name,
            List<? extends GDPropertyMetadata> propertyMetadata) {
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
    public Map<String, GDAction> getActions() {
        checkPermission(getClass(), Type.GET);
        return Collections.unmodifiableMap(action);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public GDAction getAction(String name) {
        checkPermission(getClass(), Type.GET);
        return action.get(name);
    }

    public void putAction(GDAction act) {
        checkPermission(getClass(), Type.SET);
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
        checkPermission(getClass(), Type.GET);
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath(boolean isAbsolute) {
        checkPermission(getClass(), Type.GET);
        return path + "/service/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        checkPermission(getClass(), Type.GET);
        return path + "/service/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePath(String path) {
        checkPermission(getClass(), Type.SET);
        this.path = path;
        String getPath = getPath(true);
        action.values().forEach(act -> act.updatePath(getPath));
        propertyMetadata.forEach(md -> md.updatePath(getPath));
    }

    public final boolean hasProperty(String name) {
        return properties.hasProperty(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final GDProperties getProperties() {
        checkPermission(getClass(), Type.GET);
        return properties;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<GDPropertyMetadata> getPropertiesMetadata() {
        return Collections.unmodifiableList(propertyMetadata);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String serialize(Format format) throws GDException {
        checkPermission(getClass(), Type.GET);
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
        checkPermission(getClass(), Type.GET);
        if (path == null) {
            throw new GDException(405, "Path cannot be null");
        }

        try {
            return startsWith("name", p -> getName())
                    .orBy("action", andThen(splits((first, rest) -> action.get(first).getSerializedNode(rest, format)))
                            .otherwise(() -> serializeActionList(format)))
                    .orBy("parameter", andThen(properties::getStringValue)
                            .otherwise(() -> properties.serialize(format)))
                    .otherwise(() -> serialize(format)).apply(path);
        } catch (Exception ex) {
            throw new GDException(404, "No such node found");
        }
//        if (path.isEmpty()) {
//            return serialize(format);
//        } else if (path.equals("name")) {
//            return getName();
//        } else if (path.startsWith("action")) {
//            String[] split = path.split(Constants.PATH_DELIMITER, 3);
//            if (split.length > 1 && !split[1].isEmpty()) {
//                path = "";
//                if (split.length > 2) {
//                    path = split[2];
//                }
//                GDAction act = action.get(split[1]);
//                if (act != null) {
//                    return act.getSerializedNode(path, format);
//                } else {
//                    throw new GDException(404, "No such node found");
//                }
//            } else {
//                return serializeActionList(format);
//            }
//        } else if (path.startsWith("parameter")) {
//            String[] split = path.split(Constants.PATH_DELIMITER, 2);
//            if (split.length > 1 && !split[1].isEmpty()) {
//                return properties.getStringValue(split[1]);
//            }
//            return properties.serialize(format);
//        } else {
//            throw new GDException(404, "No such node found");
//        }
    }

    private String serializeActionList(Format format)
            throws GDException {
        if (format.isJson()) {
            return getActionListJsonString(format, 0);
        } else {
            throw new GDException(405, "No such format supported");
        }
    }

    private String toJsonString(Format format, int indent) throws GDException {
        try {
            return JsonUtil.execute(mapper -> mapper.writerWithView(JsonUtil.ID.Ignore.class).writeValueAsString(this));
        } catch (IOException ex) {
            throw new GDException(ex.getMessage(), ex);
        }
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

    @JsonIgnore
    public GenericDevice getParentDevice() {
        return parentDevice;
    }
}
