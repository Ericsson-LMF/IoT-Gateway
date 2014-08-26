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

import com.ericsson.common.util.serialization.Format;
import com.ericsson.common.util.serialization.SerializationException;
import com.ericsson.common.util.serialization.SerializationUtil;
import com.ericsson.common.util.serialization.View;
import com.ericsson.deviceaccess.api.Constants;
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
        this.propertyMetadata = new ArrayList();
        if (propertyMetadata != null) {
            this.propertyMetadata.addAll(propertyMetadata);
        }
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
        try {
            return SerializationUtil.execute(format, mapper -> mapper.writerWithView(View.ID.Ignore.class).writeValueAsString(this));
        } catch (SerializationException ex) {
            throw new GDException(ex.getMessage(), ex);
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
    public String getSerializedNode(String path, Format format) throws GDException {
        checkPermission(getClass(), Type.GET);
        try {
            return SerializationUtil.serializeAccordingPath(format, path, Constants.PATH_DELIMITER, this);
        } catch (SerializationException ex) {
            throw new GDException(404, ex.getMessage(), ex);
        }
    }

    @JsonIgnore
    public GenericDevice getParentDevice() {
        return parentDevice;
    }
}
