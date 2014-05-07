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

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.api.GenericDevicePropertyMetadata;
import com.ericsson.deviceaccess.api.Serializable;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;
import com.ericsson.deviceaccess.spi.GenericDeviceError;
import com.ericsson.deviceaccess.spi.utility.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class GenericDevicePropertiesImpl extends GenericDeviceProperties.Stub
        implements GenericDeviceProperties {
    private static MetadataUtil metadataUtil = MetadataUtil.getInstance();
    public static final String LAST_UPDATE_TIME = "lastUpdateTime";
    private Map map;
    private Map metadata; // name:String -> GenericDevicePropertyMetadata
    private GenericDeviceServiceImpl parentService;

    GenericDevicePropertiesImpl(GenericDevicePropertyMetadata[] metadataArray, GenericDeviceServiceImpl parentService) {
        this.parentService = parentService;
        this.metadata = new HashMap();
        this.map = new HashMap();
        if(metadataArray != null){
        	for (int i = 0; i < metadataArray.length; i++) {
        		GenericDevicePropertyMetadata metadata = metadataArray[i];
        		this.metadata.put(metadata.getName(), metadata);
        		if (metadata.getType() == String.class) {
        			map.put(metadata.getName(), metadata.getDefaultStringValue());
        		} else {
        			map.put(metadata.getName(), metadata.getDefaultNumberValue());
        		}
        	}
        }
    }

    public GenericDevicePropertiesImpl(GenericDevicePropertyMetadata[] metadataArray) {
        this(metadataArray, null);
    }

    public boolean hasProperty(String name) {
        return map.containsKey(name);
    }

    /**
     * {@inheritDoc}
     */
    public Object getValue(String name) {
        if (map.containsKey(name)) {
            return map.get(name);
        }

        GenericDevicePropertyMetadata valueMetadata = (GenericDevicePropertyMetadata) metadata
                .get(name);
        if (Number.class.isAssignableFrom(valueMetadata.getType())) {
            return valueMetadata.getDefaultNumberValue();
        } else {
            return valueMetadata.getDefaultStringValue();
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getStringValue(String key) {
        String defaultValue = ((GenericDevicePropertyMetadata) metadata
                .get(key)).getDefaultStringValue();
        if (map.containsKey(key)) {
            Object value = map.get(key);
            if (value == null)
                return defaultValue;
            else
                return String.valueOf(value);
        }
        return defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    public void setStringValue(String key, String value) {
        if (metadata.get(key) == null) {
            throw new GenericDeviceError("There is no property: " + key
                    + " specified in the metadata for this property set.");
        }
        if (((GenericDevicePropertyMetadata) metadata.get(key)).getType() == Float.class) {
            setFloatValue(key, Float.parseFloat(value));
        } else if (((GenericDevicePropertyMetadata) metadata.get(key))
                .getType() == Integer.class) {
            setIntValue(key, Integer.parseInt(value));
        } else {
            metadataUtil.verifyPropertyAgainstMetadata(metadata, key, value);
            Object oldValue = map.get(key);
            map.put(key, value);
            tryNotifyChange(key, oldValue, value);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setIntValue(String key, int value) {
        Integer intValue = new Integer(value);
        metadataUtil.verifyPropertyAgainstMetadata(metadata, key, intValue);
        Object oldValue = map.get(key);
        map.put(key, intValue);
        tryNotifyChange(key, oldValue, intValue);
    }

    /**
     * {@inheritDoc}
     */
    public void setLongValue(String key, long value) {
        Long longValue = new Long(value);
        metadataUtil.verifyPropertyAgainstMetadata(metadata, key, longValue);
        Object oldValue = map.get(key);
        map.put(key, longValue);
        tryNotifyChange(key, oldValue, longValue);
    }

    /**
     * {@inheritDoc}
     */
    public void setFloatValue(String key, float value) {
        Float floatValue = new Float(value);
        metadataUtil.verifyPropertyAgainstMetadata(metadata, key, floatValue);
        Object oldValue = map.get(key);
        map.put(key, floatValue);
        tryNotifyChange(key, oldValue, floatValue);
    }

    private void tryNotifyChange(final String key, Object oldValue,
                                 final Object value) {
        if (metadata.containsKey(LAST_UPDATE_TIME)) {
            map.put(LAST_UPDATE_TIME, new Long(System.currentTimeMillis()));
        }
        if (parentService != null && parentService.getParentDevice() != null) {
            if ((value == null && oldValue != null) || (value != null && !value.equals(oldValue))) {
                parentService.notifyEvent(new Properties() {{
                    put(key, value);
                }});
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public int getIntValue(String key) {
        Object value = map.get(key);
        Number defaultValue = ((GenericDevicePropertyMetadata) metadata
                .get(key)).getDefaultNumberValue();
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
    public long getLongValue(String key) {
        Object value = map.get(key);
        Number defaultValue = ((GenericDevicePropertyMetadata) metadata
                .get(key)).getDefaultNumberValue();
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
    public float getFloatValue(String key) {
        Object value = map.get(key);
        Number defaultValue = ((GenericDevicePropertyMetadata) metadata
                .get(key)).getDefaultNumberValue();
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
    public String getValueType(String key) {
        if (map.containsKey(key)) {
            return ((GenericDevicePropertyMetadata) metadata.get(key)).getTypeName();
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String[] getNames() {
        return (String[]) map.keySet().toArray(new String[0]);
    }

    /**
     * {@inheritDoc}
     */
    public String serialize(int format) throws GenericDeviceException {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (format == Serializable.FORMAT_JSON
                || format == Serializable.FORMAT_JSON_WDC) {
            int indent = 0;
            return "{" + valuesToJson(format) + "}";
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }
    }

    public String serializeState() {
        StringBuffer sb = new StringBuffer("{");
        for (int i = 0; i < getNames().length; i++) {
            String name = getNames()[i];
            sb.append("\"").append(name).append("\":\"").append(Utils.escapeJSON(getStringValue(name))).append("\",");
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
    public void addAll(GenericDeviceProperties source) {
        for (int i = 0; i < source.getNames().length; i++) {
            String name = source.getNames()[i];
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

    private String valuesToJson(int format) throws GenericDeviceException {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < getNames().length; i++) {
            String name = getNames()[i];
            sb.append("\"").append(name).append("\":{");
            sb.append("\"currentValue\":\"").append(Utils.escapeJSON(getStringValue(name))).append("\",");
            if(metadata.containsKey(name)){
            	sb.append("\"metadata\":");
            	String serializedMetadata = ((GenericDevicePropertyMetadata)metadata.get(name)).serialize(format);
            	if(serializedMetadata != null && serializedMetadata.indexOf("{") >= 0){
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

    public void addDynamicProperty(GenericDevicePropertyMetadata propertyMetadata) {
        this.metadata.put(propertyMetadata.getName(), propertyMetadata);
        if (propertyMetadata.getType() == String.class) {
        	map.put(propertyMetadata.getName(), propertyMetadata.getDefaultStringValue());
        } else if (propertyMetadata.getType() == Number.class) {
        	map.put(propertyMetadata.getName(), propertyMetadata.getDefaultNumberValue());
        }
        parentService.notifyEventAdded(propertyMetadata.getName());
    }

    public void removeDynamicProperty(GenericDevicePropertyMetadata propertyMetadata) {
        this.metadata.remove(propertyMetadata.getName());
        this.map.remove(propertyMetadata.getName());
        parentService.notifyEventRemoved(propertyMetadata.getName());
    }
}
