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
import com.ericsson.deviceaccess.api.genericdevice.GDAccessPermission.Type;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import com.ericsson.deviceaccess.spi.genericdevice.GDAccessSecurity;
import com.ericsson.deviceaccess.spi.genericdevice.GDError;
import com.ericsson.deviceaccess.spi.impl.MetadataUtil;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class GDPropertiesImpl extends GDProperties.Stub
        implements GDProperties {

    public static final String LAST_UPDATE_TIME = "lastUpdateTime";
    private Map<String, Data> properties;
//    private Map<String, GDPropertyMetadata> metadata;
    private GDServiceImpl parentService;

    public GDPropertiesImpl(Iterable<GDPropertyMetadata> metadataArray, GDServiceImpl parentService) {
        this.parentService = parentService;
//        this.metadata = new HashMap<>();
        this.properties = new HashMap<>();
        if (metadataArray != null) {
            metadataArray.forEach(meta -> {
                properties.put(meta.getName(), new Data(meta).setToDefault());
            });
        }
    }

    public GDPropertiesImpl(Iterable<GDPropertyMetadata> metadataArray) {
        this(metadataArray, null);
    }

    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object getValue(String name) {
        Data data = properties.get(name);
        if (data.currentValue == null) {
            data.setToDefault();
        }
        return data.currentValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStringValue(String key) {
        Data data = properties.get(key);
        Object value = data.currentValue;
        if (value != null) {
            return String.valueOf(value);
        }
        return data.metadata.getDefaultStringValue();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStringValue(String key, String value) {
        if (properties.get(key) == null) {
            throw new GDError("There is no property: " + key
                    + " specified in the metadata for this property set.");
        }
        Class<?> type = properties.get(key).metadata.getType();
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

    @JsonAnySetter
    public void setValue(String key, Object value) {
        MetadataUtil.INSTANCE.verifyPropertyAgainstMetadata(properties.get(key).metadata, key, value);
        properties.compute(key, (k, data) -> {
            tryNotifyChange(k, data.currentValue, value);
            data.currentValue = value;
            return data;
        });
    }

    private void tryNotifyChange(final String key, Object oldValue,
            final Object value) {
        properties.computeIfPresent(LAST_UPDATE_TIME, (k, data) -> {
            data.currentValue = System.currentTimeMillis();
            return data;
        });
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
        return getValue(key, Integer::parseInt, value -> value.intValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getLongValue(String key) {
        return getValue(key, Long::parseLong, value -> value.longValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getFloatValue(String key) {
        return getValue(key, Float::parseFloat, value -> value.floatValue());
    }

    private <T extends Number> T getValue(String key, Function<String, T> parser, Function<Number, T> getter) {
        Data data = properties.get(key);
        Object value = data.currentValue;
        Number defaultValue = data.metadata.getDefaultNumberValue();
        if (value instanceof String) {
            try {
                return parser.apply((String) value);
            } catch (NumberFormatException e) {
                return getter.apply(defaultValue);
            }
        } else if (value instanceof Number) {
            return getter.apply((Number) value);
        }
        return getter.apply(defaultValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueType(String key) {
        if (properties.containsKey(key)) {
            return properties.get(key).metadata.getTypeName();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Data> getProperties() {
        return Collections.unmodifiableMap(properties);
    }

    @Override
    public String serialize(Format format) throws GDException {
        GDAccessSecurity.checkPermission(getClass(), Type.GET);
        try {
            return SerializationUtil.execute(format, mapper -> mapper.writerWithView(View.ID.Ignore.class).writeValueAsString(this));
        } catch (SerializationException ex) {
            throw new GDException(ex.getMessage(), ex);
        }
    }

    @Override
    public String serializeState() {
        try {
            return SerializationUtil.execute(Format.JSON, mapper -> mapper.writerWithView(View.StatelessID.Ignore.class).writeValueAsString(this));
        } catch (SerializationException ex) {
            return "{}";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addAll(GDProperties source) {
        source.getProperties().forEach((name, data) -> {
            Class type = data.metadata.getType();
            if (String.class.isAssignableFrom(type)) {
                setStringValue(name, source.getStringValue(name));
            } else if (Integer.class.isAssignableFrom(type)) {
                setIntValue(name, source.getIntValue(name));
            } else if (Float.class.isAssignableFrom(type)) {
                setFloatValue(name, source.getFloatValue(name));
            }
        });
    }

    public void addDynamicProperty(GDPropertyMetadata propertyMetadata) {
        properties.put(propertyMetadata.getName(), new Data(propertyMetadata).setToDefault());
        parentService.notifyEventAdded(propertyMetadata.getName());
    }

    public void removeDynamicProperty(GDPropertyMetadata propertyMetadata) {
        this.properties.remove(propertyMetadata.getName());
        parentService.notifyEventRemoved(propertyMetadata.getName());
    }
}
