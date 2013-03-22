/*
 * Copyright (c) Ericsson AB, 2011.
 *
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 *
 * ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ERICSSON SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.ericsson.deviceaccess.spi.schema;

import java.util.Arrays;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDevicePropertyMetadata;
import com.ericsson.deviceaccess.api.Serializable;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;
import com.ericsson.deviceaccess.spi.utility.Utils;

/**
 * 
 */
public class ParameterSchema implements GenericDevicePropertyMetadata {

    private String name;
    private Number minValue;
    private Number maxValue;
    private Object defaultValue;
    private Class type;
    private String[] validValues;
    private String path;

    /**
     * @param name
     * @param type one of {@link String.class}, {@link Integer.class}, {@link Long.class} or {@link Float.class}
     * @param defaultValue
     */
    private ParameterSchema(String name, Class type, Object defaultValue) {
        if (!(Long.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type) || String.class.isAssignableFrom(type))) {
            throw new ServiceSchemaError("Parameter must be of type Long, Integer, Float or String");
        }
        
        if (defaultValue != null && !type.isAssignableFrom(defaultValue.getClass())) {
            throw new ServiceSchemaError("Default value is not of type: " + type);
        }
        
        if (Float.class.isAssignableFrom(type)) {
            minValue = new Float(Float.NEGATIVE_INFINITY);
            maxValue = new Float(Float.POSITIVE_INFINITY);
        } else if (Integer.class.isAssignableFrom(type)) {
            minValue = new Integer(Integer.MIN_VALUE);
            maxValue = new Integer(Integer.MAX_VALUE); 
        } else if (Long.class.isAssignableFrom(type)) {
            minValue = new Long(Long.MIN_VALUE);
            maxValue = new Long(Long.MAX_VALUE);
        }

        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    public Class getType() {
        return type;
    }
    
    /**
     * {@inheritDoc}
     */
	public String getTypeName() {
		if (type == String.class) {
			return "String";
		} else if (type == Float.class) {
			return "Float";
		} else if (type == Integer.class) {
			return "Integer";
        } else if (type == Long.class) {
            return "Long";
		} else {
			//TODO: Should we cast an exception if the argument type is not string, float or integer?
			return type.getName();
		}
	}

    /**
     * {@inheritDoc}
     */
    public Number getMinValue() {
        return minValue;
    }

    /**
     * {@inheritDoc}
     */
    public Number getMaxValue() {
        return maxValue;
    }

    /**
     * {@inheritDoc}
     */
    public Number getDefaultNumberValue() {
        return (Number) defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    public String getDefaultStringValue() {
        return defaultValue + "";
    }

    /**
     * {@inheritDoc}
     */
    public String[] getValidValues() {
        return validValues;
    }

    /**
     * {@inheritDoc}
     */
    public String serialize(int format) throws GenericDeviceException {
        if (format == Serializable.FORMAT_JSON || format == Serializable.FORMAT_JSON_WDC) {
            return toJsonString(format, 0);
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getSerializedNode(String path, int format) throws GenericDeviceException {
        if (path == null)
            throw new GenericDeviceException(405, "Path cannot be null");

        if (path.length() == 0) {
            return serialize(format);
        } else {
            return getDefaultStringValue();
        }
    }

    private String toJsonString(int format, int indent) {
        StringBuffer json = new StringBuffer("{");
        json.append("\"name\":\"").append(Utils.escapeJSON(name)).append("\",");
        json.append("\"type\":\"").append(getTypeName()).append("\",");
        if (minValue != null) {
            json.append("\"minValue\":\"").append(minValue).append("\",");
        }
        if (maxValue != null) {
            json.append("\"maxValue\":\"").append(maxValue).append("\",");
        }

        if (defaultValue != null) {
            json.append("\"defaultValue\":\"").append(Utils.escapeJSON(defaultValue.toString())).append("\",");
        }
        if (validValues != null) {
            json.append("\"validValues\":[");
            for (int i = 0; i < validValues.length; i++) {
                String value = Utils.escapeJSON(validValues[i]);
                json.append('"').append(value).append('"');
                if (i < validValues.length - 1) {
                    json.append(',');
                }
            }
            json.append("],");
        }
        // remove last ','
        json.setLength(json.length()-1);
        json.append("}");
        return json.toString();
    }

    /**
     * {@inheritDoc}
     */
    public String getPath(boolean isAbsolute) {
        return path + "/parameter/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    public String getPath() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return path + "/parameter/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    public void updatePath(String path) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.path = path;
    }
    
    public static class Builder {
        private String name;
        private Number minValue;
        private Number maxValue;
        private Object defaultValue;
        private Class type = String.class;
        private String[] validValues;
        
        /**
         * Creates a builder for a parameter with the specified name.
         * @param parameterName
         * @return the builder
         */
        public Builder(String parameterName) {
            this.name = parameterName;
        }
        
        /**
         * Sets the min value. 
         * The string will be interpreted according to the type of the parameter schema.
         * 
         * @param minValue
         * @return this parameter schema
         */
        public Builder setMinValue(String minValue) {
            if (Float.class.isAssignableFrom(type)) {
                this.minValue = Float.valueOf(minValue);
            } else if (Integer.class.isAssignableFrom(type)) {
                this.minValue = Integer.decode(minValue);
            } else if (Long.class.isAssignableFrom(type)) {
                this.minValue = Long.decode(minValue);
            } else {
                throw new ServiceSchemaError("Cannot set min value when the parameter type is "+type);
            }
            return this;
        }
       
        /**
         * Sets the max value. The string will be interpreted according to the type of the parameter schema.
         * 
         * @param maxValue
         * @return this parameter schema
         */
        public Builder setMaxValue(String maxValue) {
            if (Float.class.isAssignableFrom(type)) {
                this.maxValue = Float.valueOf(maxValue);
            } else if (Integer.class.isAssignableFrom(type)) {
                this.maxValue = Integer.decode(maxValue);
            } else if (Long.class.isAssignableFrom(type)) {
                this.maxValue = Long.decode(maxValue);
            } else {
                throw new ServiceSchemaError("Cannot set max value when the parameter type is "+type);
            }
            return this;
        }
        
        /**
         * @param defaultValue the defaultValue to set
         * @return 
         * @return the builder
         */
        public Builder setDefaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }
        
        /**
         * @param type the type to set
         * @return 
         * @return the builder
         */
        public Builder setType(Class type) {
            this.type = type;
            return this;
        }
        
        /**
         * @param validValues the validValues to set
         * @return 
         * @return the builder
         */
        public Builder setValidValues(String[] validValues) {
            this.validValues = validValues;
            return this;
        }
        
        /**
         * Builds the schema.
         * @return the built schema
         */
        public ParameterSchema build() {
            if (name == null) {
                throw new ServiceSchemaError("Name must be specified");
            }

            if (!(Long.class.isAssignableFrom(type) || Integer.class.isAssignableFrom(type) || Float.class.isAssignableFrom(type) || String.class.isAssignableFrom(type))) {
                throw new ServiceSchemaError("Parameter must be of type Long, Integer, Float or String");
            }
            
            if (defaultValue != null && !type.isAssignableFrom(defaultValue.getClass())) {
                throw new ServiceSchemaError("Default value is not of type: " + type);
            }
            
            ParameterSchema parameterSchema = new ParameterSchema(name, type, defaultValue);

            if (Float.class.isAssignableFrom(type)) {
                minValue = minValue == null ? new Float(Float.NEGATIVE_INFINITY) : minValue;
                if (!Float.class.isAssignableFrom(minValue.getClass())) {
                    throw new ServiceSchemaError("Min value type ("+minValue.getClass()+") does not match parameter type ("+type+")");
                }
                parameterSchema.minValue = minValue;
                maxValue = maxValue == null ? new Float(Float.POSITIVE_INFINITY) : maxValue;
                if (!Float.class.isAssignableFrom(maxValue.getClass())) {
                    throw new ServiceSchemaError("Max value type ("+maxValue.getClass()+") does not match parameter type ("+type+")");
                }
                parameterSchema.maxValue = maxValue;
            } else if (Integer.class.isAssignableFrom(type)) {
                minValue = minValue == null ? new Integer(Integer.MIN_VALUE) : minValue;
                if (!Integer.class.isAssignableFrom(minValue.getClass())) {
                    throw new ServiceSchemaError("Min value type ("+minValue.getClass()+") does not match parameter type ("+type+")");
                }
                parameterSchema.minValue = minValue;
                maxValue = maxValue == null ? new Integer(Integer.MAX_VALUE) : maxValue; 
                if (!Integer.class.isAssignableFrom(maxValue.getClass())) {
                    throw new ServiceSchemaError("Max value type ("+maxValue.getClass()+") does not match parameter type ("+type+")");
                }
                parameterSchema.maxValue = maxValue;
            } else if (Long.class.isAssignableFrom(type)) {
                minValue = minValue == null ? new Long(Long.MIN_VALUE) : minValue;
                if (!Long.class.isAssignableFrom(minValue.getClass())) {
                    throw new ServiceSchemaError("Min value type ("+minValue.getClass()+") does not match parameter type ("+type+")");
                }
                parameterSchema.minValue = minValue;
                maxValue = maxValue == null ? new Long(Long.MAX_VALUE) : maxValue;
                if (!Long.class.isAssignableFrom(maxValue.getClass())) {
                    throw new ServiceSchemaError("Max value type ("+maxValue.getClass()+") does not match parameter type ("+type+")");
                }
                parameterSchema.maxValue = maxValue;
            }
            
            if (String.class.isAssignableFrom(type)) {
                if (validValues != null) {
                    if (defaultValue == null) {
                        throw new ServiceSchemaError("Default value ("+defaultValue+") is not among valid values.");
                    }
                    Arrays.sort(validValues);
                    if (Arrays.binarySearch(validValues, defaultValue) < 0) {
                        throw new ServiceSchemaError("Default value ("+defaultValue+") is not among valid values.");
                    }
                    parameterSchema.validValues = validValues;
                }
            } else {
                if (validValues != null) {
                    throw new ServiceSchemaError("Valid values are set on a non-string schema");
                }
            }
            
            return parameterSchema;
        }
    }
}
