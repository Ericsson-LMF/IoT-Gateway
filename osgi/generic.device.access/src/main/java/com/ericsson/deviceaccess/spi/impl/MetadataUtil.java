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

package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.*;
import com.ericsson.deviceaccess.spi.GenericDeviceError;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Checker for checking {@link GenericDeviceProperties} against {@link GenericDevicePropertyMetadata}.
 */
class MetadataUtil {
    private static MetadataUtil instance = new MetadataUtil();

    private MetadataUtil() {
    }

    static MetadataUtil getInstance() {
        return instance;
    }

    /**
     * Verifies all the specified properties against the specified metadata.
     *
     * @param props
     * @param metadataMap name:String -> {@link GenericDevicePropertyMetadata}
     * @throws GenericDeviceError thrown if value does not adhere to the metadata
     */
    void verifyPropertiesAgainstMetadata(GenericDeviceProperties props, Map metadataMap) throws GenericDeviceError {
        if (metadataMap == null) {
            return;
        }

        String[] names = props.getNames();
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            verifyPropertyAgainstMetadata(metadataMap, name, props.getValue(name));
        }
    }

    /**
     * Verifies the specified property, with the specified name, against the specified metadata.
     *
     * @param metadataMap   name:String -> {@link GenericDevicePropertyMetadata}
     * @param propertyName
     * @param propertyValue
     * @throws GenericDeviceError thrown if value does not adhere to the metadata
     */
    void verifyPropertyAgainstMetadata(Map metadataMap, String propertyName, Object propertyValue) throws GenericDeviceError {
        GenericDevicePropertyMetadata metadata = (GenericDevicePropertyMetadata) metadataMap.get(propertyName);
        
        if(metadata == null) return;
        
        if (Number.class.isAssignableFrom(metadata.getType())) {
            if (propertyValue instanceof String) {
                try {
                    Float.parseFloat((String) propertyValue);
                } catch (NumberFormatException e) {
                    throw new GenericDeviceError(
                            "The property: '" + propertyName + "'=" + propertyValue + " is a '" + propertyValue.getClass() +
                                    "' which not parsable to '" + metadata.getType() + "'");
                }
            } else if (Float.class.isAssignableFrom(metadata.getType())) {
                if (!(propertyValue instanceof Float)) {
                    throw new GenericDeviceError(
                            "The property: '" + propertyName + "'=" + propertyValue + " is a '" + propertyValue.getClass() +
                                    "' which not assignable to '" + metadata.getType() + "'");
                }
            } else if (Long.class.isAssignableFrom(metadata.getType())) {
                if (!(propertyValue instanceof Long)) {
                    throw new GenericDeviceError(
                            "The property: '" + propertyName + "'=" + propertyValue + " is a '" + propertyValue.getClass() +
                                    "' which not assignable to '" + metadata.getType() + "'");
                }
            } else {
                if (!(propertyValue instanceof Integer || propertyValue instanceof Short || propertyValue instanceof Byte)) {
                    throw new GenericDeviceError(
                            "The property: '" + propertyName + "'=" + propertyValue + " is a '" + propertyValue.getClass() +
                                    "' which not assignable to '" + metadata.getType() + "'");
                }
            }

            double doubleValue;
            if (propertyValue instanceof String) {
                doubleValue = Double.parseDouble((String) propertyValue);
            } else {
                doubleValue = ((Number) propertyValue).doubleValue();
            }
            checkRange(metadata, propertyName, propertyValue, doubleValue);
        } else {
            // Treat all other objects as string
            String value = propertyValue.toString();
            String[] validValues = metadata.getValidValues();
            if (validValues != null) {
                Arrays.sort(validValues);
                if (Arrays.binarySearch(validValues, value) < 0) {
                    throw new GenericDeviceError(
                            "The property: '" + propertyName + "'=" + propertyValue + " with the value '" + value +
                                    "' is not among the allowed values '" + arrayToString(validValues) + "'");
                }
            }
        }
    }

    /**
     * Checks that the specified value is within the range specified in the metadata.
     *
     * @param metadata
     * @param propertyName
     * @param propertyValue
     * @param valueToBeChecked
     * @throws GenericDeviceError thrown if value does not adhere to the metadata
     */
    private static void checkRange(GenericDevicePropertyMetadata metadata, String propertyName, Object propertyValue, double valueToBeChecked) throws GenericDeviceError {
        if (valueToBeChecked > metadata.getMaxValue().doubleValue()) {
            throw new GenericDeviceError(
                    "The property: '" + propertyName + "'=" + propertyValue +
                            " is above the max value '" + metadata.getMaxValue() + "'");
        }

        if (valueToBeChecked < metadata.getMinValue().doubleValue()) {
            throw new GenericDeviceError(
                    "The property: '" + propertyName + "'=" + propertyValue +
                            " is below the min value '" + metadata.getMinValue() + "'");
        }
    }

    /**
     * Makes a string of the specified array
     *
     * @param validValues
     * @return the array
     */
    private static String arrayToString(String[] validValues) {
        StringBuffer sb = new StringBuffer();
        sb.append('[');
        for (int i = 0; i < validValues.length; i++) {
            String string = validValues[i];
            sb.append(string).append(',');
        }
        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }
        sb.append(']');
        return sb.toString();
    }

    /**
     * Makes a JSON string of the specified metadata.
     *
     * @param path
     * @param format
     * @param name
     * @param metadata
     * @return JSON string of the specified metadata
     * @throws GenericDeviceException
     */
    String metadataToJson(String path, int format, String name, Collection metadata) throws GenericDeviceException {
        String retVal = "";
        if (path == null) {
            path = "";
        }
        if (format == Serializable.FORMAT_JSON || format == Serializable.FORMAT_JSON_WDC) {
            if (metadata.size() > 0) {
                StringBuffer sb = new StringBuffer();
                sb.append('"').append(name).append("\": {");
                for (Iterator iterator = metadata.iterator(); iterator.hasNext(); ) {
                    GenericDevicePropertyMetadata md = (GenericDevicePropertyMetadata) iterator.next();
                    if (path.indexOf(Constants.PATH_DELIMITER) > 0) {
                        sb.append('"').append(md.getName()).append("\":").append(md.getSerializedNode(path.substring(path.indexOf(Constants.PATH_DELIMITER)), format));
                    } else {
                        sb.append('"').append(md.getName()).append("\":").append(md.getSerializedNode("", format));
                    }
                    sb.append(',');
                }
                sb.setLength(sb.length() - 1);
                sb.append('}');
                retVal = sb.toString();
            }
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }

        return retVal;
    }
}
