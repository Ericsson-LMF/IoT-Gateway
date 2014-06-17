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

import com.ericsson.deviceaccess.api.genericdevice.GDAccessPermission.Type;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity;
import com.ericsson.deviceaccess.spi.genericdevice.GDError;
import com.ericsson.deviceaccess.spi.impl.MetadataUtil;
import com.ericsson.commonutil.StringUtil;
import java.util.HashMap;
import java.util.Map;

public class GDPropertiesImpl extends GDProperties.Stub
        implements GDProperties {

    public static final String LAST_UPDATE_TIME = "lastUpdateTime";
    private Map<String, Object> map;
    private Map<String, GDPropertyMetadata> metadata;
    private GDServiceImpl parentService;

    public GDPropertiesImpl(Iterable<GDPropertyMetadata> metadataArray, GDServiceImpl parentService) {
        this.parentService = parentService;
        this.metadata = new HashMap<>();
        this.map = new HashMap<>();
        if (metadataArray != null) {
            metadataArray.forEach(meta -> {
                String name = meta.getName();
                metadata.put(name, meta);
                if (String.class.equals(meta.getType())) {
                    map.put(name, meta.getDefaultStringValue());
                } else {
                    map.put(name, meta.getDefaultNumberValue());
                }
            });
        }
    }

    public GDPropertiesImpl(Iterable<GDPropertyMetadata> metadataArray) {
        this(metadataArray, null);
    }

    @Override
    public boolean hasProperty(String name) {
        return map.containsKey(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String name) {
        return map.computeIfAbsent(name, key -> {
            GDPropertyMetadata meta = metadata.get(key);
            if (String.class.equals(meta.getType())) {
                return meta.getDefaultStringValue();
            }
            return meta.getDefaultNumberValue();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue(String key) {
        String defaultValue = metadata.get(key).getDefaultStringValue();
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value != null) {
                return String.valueOf(value);
            }
        }
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStringValue(String key, String value) {
        if (metadata.get(key) == null) {
            throw new GDError("There is no property: " + key
                    + " specified in the metadata for this property set.");
        }
        Class<?> type = metadata.get(key).getType();
        if (Float.class.equals(type)) {
            setFloatValue(key, Float.parseFloat(value));
        } else if (Integer.class.equals(type)) {
            setIntValue(key, Integer.parseInt(value));
        } else {
            setValue(key, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIntValue(String key, int value) {
        setValue(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLongValue(String key, long value) {
        setValue(key, value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setFloatValue(String key, float value) {
        setValue(key, value);
    }

    private void setValue(String key, Object value) {
        MetadataUtil.INSTANCE.verifyPropertyAgainstMetadata(metadata, key, value);
        map.compute(key, (k, oldValue) -> {
            tryNotifyChange(k, oldValue, value);
            return value;
        });
    }

    private void tryNotifyChange(final String key, Object oldValue,
            final Object value) {
        if (metadata.containsKey(LAST_UPDATE_TIME)) {
            map.put(LAST_UPDATE_TIME, System.currentTimeMillis());
        }
        if (parentService != null && parentService.getParentDevice() != null) {
            if ((value == null && oldValue != null) || (value != null && !value.equals(oldValue))) {
                parentService.notifyEvent(new HashMap() {
                    {
                        put(key, value);
                    }
                });
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getIntValue(String key) {
        Object value = map.get(key);
        Number defaultValue = metadata.get(key).getDefaultNumberValue();
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return defaultValue.intValue();
            }
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLongValue(String key) {
        Object value = map.get(key);
        Number defaultValue = metadata.get(key).getDefaultNumberValue();
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return defaultValue.intValue();
            }
        } else if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue.intValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloatValue(String key) {
        Object value = map.get(key);
        Number defaultValue = metadata.get(key).getDefaultNumberValue();
        if (value instanceof String) {
            try {
                return Float.parseFloat((String) value);
            } catch (NumberFormatException e) {
                return defaultValue.floatValue();
            }
        } else if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        return defaultValue.floatValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType(String key) {
        if (map.containsKey(key)) {
            return metadata.get(key).getTypeName();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getNames() {
        return map.keySet().toArray(new String[0]);
    }

    @Override
    public String serialize(Format format) throws GDException {
        GDAccessSecurity.checkPermission(getClass(), Type.GET);
        if (format.isJson()) {
            int indent = 0;
            return "{" + valuesToJson(format) + "}";
        } else {
            throw new GDException(405, "No such format supported");
        }
    }

    @Override
    public String serializeState() {
        StringBuilder sb = new StringBuilder("{");
        for (String name : getNames()) {
            sb.append("\"").append(name).append("\":\"").append(StringUtil.escapeJSON(getStringValue(name))).append("\",");
        }

        // remove last ','
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        sb.append("}");

        return sb.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAll(GDProperties source) {
        for (String name : source.getNames()) {
            if (String.class.getName().equals(source.getValueType(name))) {
                setStringValue(name, source.getStringValue(name));
            } else if (Integer.class.getName()
                    .equals(source.getValueType(name))) {
                setIntValue(name, source.getIntValue(name));
            } else if (Float.class.getName().equals(source.getValueType(name))) {
                setFloatValue(name, source.getFloatValue(name));
            }
        }
    }

    private String valuesToJson(Format format) throws GDException {
        StringBuilder sb = new StringBuilder();
        for (String name : getNames()) {
            sb.append("\"").append(name).append("\":{");
            sb.append("\"currentValue\":\"").append(StringUtil.escapeJSON(getStringValue(name))).append("\",");
            if (metadata.containsKey(name)) {
                sb.append("\"metadata\":");
                String serializedMetadata = metadata.get(name).serialize(format);
                if (serializedMetadata != null && serializedMetadata.contains("{")) {
                    sb.append(serializedMetadata);
                } else {
                    sb.append("null");
                }
            } else {
                sb.append("\"metadata\":null");
            }
            sb.append("},");
        }

        // remove last ','
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    public void addDynamicProperty(GDPropertyMetadata propertyMetadata) {
        this.metadata.put(propertyMetadata.getName(), propertyMetadata);
        if (String.class.equals(propertyMetadata.getType())) {
            map.put(propertyMetadata.getName(), propertyMetadata.getDefaultStringValue());
        } else if (Number.class.isAssignableFrom(propertyMetadata.getType())) {
            map.put(propertyMetadata.getName(), propertyMetadata.getDefaultNumberValue());
        }
        parentService.notifyEventAdded(propertyMetadata.getName());
    }

    public void removeDynamicProperty(GDPropertyMetadata propertyMetadata) {
        this.metadata.remove(propertyMetadata.getName());
        this.map.remove(propertyMetadata.getName());
        parentService.notifyEventRemoved(propertyMetadata.getName());
    }
}
