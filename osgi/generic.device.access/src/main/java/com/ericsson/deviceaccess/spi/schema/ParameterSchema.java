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
package com.ericsson.deviceaccess.spi.schema;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDevicePropertyMetadata;
import com.ericsson.deviceaccess.api.Serializable;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;
import com.ericsson.deviceaccess.spi.utility.Utils;
import java.util.Arrays;

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
     * @param type one of
     * {@link String.class}, {@link Integer.class}, {@link Long.class} or
     * {@link Float.class}
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
            minValue = Float.NEGATIVE_INFINITY;
            maxValue = Float.POSITIVE_INFINITY;
        } else if (Integer.class.isAssignableFrom(type)) {
            minValue = Integer.MIN_VALUE;
            maxValue = Integer.MAX_VALUE;
        } else if (Long.class.isAssignableFrom(type)) {
            minValue = Long.MIN_VALUE;
            maxValue = Long.MAX_VALUE;
        }

        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class getType() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public Number getMinValue() {
        return minValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number getMaxValue() {
        return maxValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Number getDefaultNumberValue() {
        return (Number) defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDefaultStringValue() {
        return defaultValue + "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] getValidValues() {
        return validValues;
    }

    /**
     * {@inheritDoc}
     */
    @Override
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
    @Override
    public String getSerializedNode(String path, int format) throws GenericDeviceException {
        if (path == null) {
            throw new GenericDeviceException(405, "Path cannot be null");
        }

        if (path.length() == 0) {
            return serialize(format);
        } else {
            return getDefaultStringValue();
        }
    }

    private String toJsonString(int format, int indent) {
        StringBuilder json = new StringBuilder("{");
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
        json.setLength(json.length() - 1);
        json.append("}");
        return json.toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath(boolean isAbsolute) {
        return path + "/parameter/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPath() {
        GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        return path + "/parameter/" + this.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updatePath(String path) {
        GenericDeviceAccessSecurity.checkSetPermission(getClass().getName());
        this.path = path;
    }

    public static class Builder {

        private String name;
        private Number minValue;
        private Number maxValue;
        private Object defaultValue;
        private Class type;
        private String[] validValues;

        /**
         * Creates a builder for a parameter with the specified name.
         *
         */
        public Builder() {
        }

        public Builder(String name, Class<?> type) {
            this.name = name;
            this.type = type;
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setType(Class type) {
            this.type = type;
            return this;
        }

        /**
         * Sets the min value. The string will be interpreted according to the
         * type of the parameter schema.
         *
         * @param minValue
         * @return this parameter schema
         */
        public Builder setMinValue(String minValue) {
            if (isSuperOf(Float.class, type)) {
                this.minValue = Float.valueOf(minValue);
            } else if (isSuperOf(Integer.class, type)) {
                this.minValue = Integer.decode(minValue);
            } else if (isSuperOf(Long.class, type)) {
                this.minValue = Long.decode(minValue);
            } else {
                throw new ServiceSchemaError("Cannot set min value when the parameter type is " + type);
            }
            return this;
        }

        /**
         * Sets the max value. The string will be interpreted according to the
         * type of the parameter schema.
         *
         * @param maxValue
         * @return this parameter schema
         */
        public Builder setMaxValue(String maxValue) {
            if (isSuperOf(Float.class, type)) {
                this.maxValue = Float.valueOf(maxValue);
            } else if (isSuperOf(Integer.class, type)) {
                this.maxValue = Integer.decode(maxValue);
            } else if (isSuperOf(Long.class, type)) {
                this.maxValue = Long.decode(maxValue);
            } else {
                throw new ServiceSchemaError("Cannot set max value when the parameter type is " + type);
            }
            return this;
        }

        /**
         * @param defaultValue the defaultValue to set
         * @return the builder
         */
        public Builder setDefault(String defaultValue) {
            try {
                if (isSuperOf(Float.class, type)) {
                    this.defaultValue = Float.valueOf(defaultValue);
                } else if (isSuperOf(Integer.class, type)) {
                    this.defaultValue = Integer.decode(defaultValue);
                } else if (isSuperOf(Long.class, type)) {
                    this.defaultValue = Long.decode(defaultValue);
                } else {
                    this.defaultValue = defaultValue;
                }
            } catch (NumberFormatException ex) {
                throw new ServiceSchemaError(ex);
            }
            return this;
        }

        /**
         * @param validValues the validValues to set
         * @return the builder
         */
        public Builder setValidValues(String... validValues) {
            this.validValues = validValues;
            return this;
        }

        /**
         * Builds the schema.
         *
         * @return the built schema
         */
        public ParameterSchema build() {
            if (name == null) {
                throw new ServiceSchemaError("Name must be specified");
            }

            if (!(isSuperOf(Long.class, type) || isSuperOf(Integer.class, type) || isSuperOf(Float.class, type) || isSuperOf(String.class, type))) {
                throw new ServiceSchemaError("Parameter must be of type Long, Integer, Float or String");
            }

            if (defaultValue == null) {
                if (isSuperOf(String.class, type)) {
                    defaultValue = "";
                } else if (isSuperOf(Integer.class, type)) {
                    defaultValue = 0;
                } else if (isSuperOf(Long.class, type)) {
                    defaultValue = 0l;
                } else if (isSuperOf(Float.class, type)) {
                    defaultValue = 0.0f;
                }
            }

            ParameterSchema parameterSchema = new ParameterSchema(name, type, defaultValue);

            if (isSuperOf(Float.class, type)) {
                minValue = minValue == null ? new Float(Float.NEGATIVE_INFINITY) : minValue;
                if (!isSuperOf(Float.class, minValue.getClass())) {
                    throw new ServiceSchemaError("Min value type (" + minValue.getClass() + ") does not match parameter type (" + type + ")");
                }
                parameterSchema.minValue = minValue;
                maxValue = maxValue == null ? new Float(Float.POSITIVE_INFINITY) : maxValue;
                if (!isSuperOf(Float.class, maxValue.getClass())) {
                    throw new ServiceSchemaError("Max value type (" + maxValue.getClass() + ") does not match parameter type (" + type + ")");
                }
                parameterSchema.maxValue = maxValue;
            } else if (isSuperOf(Integer.class, type)) {
                minValue = minValue == null ? new Integer(Integer.MIN_VALUE) : minValue;
                if (!isSuperOf(Integer.class, minValue.getClass())) {
                    throw new ServiceSchemaError("Min value type (" + minValue.getClass() + ") does not match parameter type (" + type + ")");
                }
                parameterSchema.minValue = minValue;
                maxValue = maxValue == null ? new Integer(Integer.MAX_VALUE) : maxValue;
                if (!isSuperOf(Integer.class, maxValue.getClass())) {
                    throw new ServiceSchemaError("Max value type (" + maxValue.getClass() + ") does not match parameter type (" + type + ")");
                }
                parameterSchema.maxValue = maxValue;
            } else if (isSuperOf(Long.class, type)) {
                minValue = minValue == null ? new Long(Long.MIN_VALUE) : minValue;
                if (!isSuperOf(Long.class, minValue.getClass())) {
                    throw new ServiceSchemaError("Min value type (" + minValue.getClass() + ") does not match parameter type (" + type + ")");
                }
                parameterSchema.minValue = minValue;
                maxValue = maxValue == null ? new Long(Long.MAX_VALUE) : maxValue;
                if (!isSuperOf(Long.class, maxValue.getClass())) {
                    throw new ServiceSchemaError("Max value type (" + maxValue.getClass() + ") does not match parameter type (" + type + ")");
                }
                parameterSchema.maxValue = maxValue;
            }

            if (isSuperOf(String.class, type)) {
                if (validValues != null) {
                    Arrays.sort(validValues);
                    if (Arrays.binarySearch(validValues, defaultValue) < 0) {
                        throw new ServiceSchemaError("Default value (" + defaultValue + ") is not among valid values.");
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

        private boolean isSuperOf(Class a, Class b) {
            return a.isAssignableFrom(b);
        }
    }
}
