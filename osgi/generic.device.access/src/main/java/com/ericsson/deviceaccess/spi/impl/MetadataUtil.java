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
import com.ericsson.deviceaccess.api.Serializable.Format;
import com.ericsson.deviceaccess.api.genericdevice.GDException;
import com.ericsson.deviceaccess.api.genericdevice.GDProperties;
import com.ericsson.deviceaccess.api.genericdevice.GDPropertyMetadata;
import com.ericsson.deviceaccess.spi.genericdevice.GDError;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Checker for checking {@link GDProperties} against {@link GDPropertyMetadata}.
 */
public enum MetadataUtil {

    /**
     * Singleton.
     */
    INSTANCE;

    /**
     * Verifies all the specified properties against the specified metadata.
     *
     * @param props
     * @param metadataMap name:String -> {@link GDPropertyMetadata}
     * @throws GDError thrown if value does not adhere to the metadata
     */
    public void verifyPropertiesAgainstMetadata(GDProperties props, Map<String, GDPropertyMetadata> metadataMap) throws GDError {
        if (metadataMap == null) {
            return;
        }
        for (String name : props.getProperties().keySet()) {
            verifyPropertyAgainstMetadata(metadataMap.get(name), name, props.getValue(name));
        }
    }

    /**
     * Verifies the specified property, with the specified name, against the
     * specified metadata.
     *
     * @param metadata
     * @param propertyName
     * @param propertyValue
     * @throws GDError thrown if value does not adhere to the metadata
     */
    public void verifyPropertyAgainstMetadata(GDPropertyMetadata metadata, String propertyName, Object propertyValue) throws GDError {
        if (metadata == null) {
            return;
        }

        Class<?> type = metadata.getType();
        if (Number.class.isAssignableFrom(type)) {
            if (propertyValue instanceof String) {
                try {
                    Float.parseFloat((String) propertyValue);
                } catch (NumberFormatException e) {
                    throwError(propertyName, propertyValue, type);
                }
            } else if (Float.class.isAssignableFrom(type)) {
                if (!(propertyValue instanceof Float)) {
                    throwError(propertyName, propertyValue, type);
                }
            } else if (Long.class.isAssignableFrom(type)) {
                if (!(propertyValue instanceof Long)) {
                    throwError(propertyName, propertyValue, type);
                }
            } else {
                if (!(propertyValue instanceof Integer || propertyValue instanceof Short || propertyValue instanceof Byte)) {
                    throwError(propertyName, propertyValue, type);
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
                    throw new GDError(
                            "The property: '" + propertyName + "'=" + propertyValue + " with the value '" + value
                            + "' is not among the allowed values '" + Arrays.toString(validValues) + "'");
                }
            }
        }
    }

    private void throwError(String propertyName, Object propertyValue, Class<?> type) throws GDError {
        StringBuilder builder = new StringBuilder();
        builder.append("The property: ")
                .append("'").append(propertyName).append("'")
                .append("=")
                .append(propertyValue)
                .append(" is a ")
                .append("'").append(propertyValue.getClass()).append("'")
                .append(" which not parsable to ")
                .append("'").append(type).append("'");
        throw new GDError(builder.toString());
    }

    /**
     * Checks that the specified value is within the range specified in the
     * metadata.
     *
     * @param metadata
     * @param propertyName
     * @param propertyValue
     * @param valueToBeChecked
     * @throws GDError thrown if value does not adhere to the metadata
     */
    private static void checkRange(GDPropertyMetadata metadata, String propertyName, Object propertyValue, double valueToBeChecked) throws GDError {
        if (valueToBeChecked > metadata.getMaxValue().doubleValue()) {
            throw new GDError(
                    "The property: '" + propertyName + "'=" + propertyValue
                    + " is above the max value '" + metadata.getMaxValue() + "'");
        }

        if (valueToBeChecked < metadata.getMinValue().doubleValue()) {
            throw new GDError(
                    "The property: '" + propertyName + "'=" + propertyValue
                    + " is below the min value '" + metadata.getMinValue() + "'");
        }
    }

    /**
     * Makes a JSON string of the specified metadata.
     *
     * @param path
     * @param format
     * @param name
     * @param metadata
     * @return JSON string of the specified metadata
     * @throws GDException
     */
    public String metadataToJson(String path, Format format, String name, Collection<GDPropertyMetadata> metadata) throws GDException {
        String retVal = "";
        if (path == null) {
            path = "";
        }
        if (format.isJson()) {
            if (metadata.size() > 0) {
                StringBuilder builder = new StringBuilder();
                builder.append('"').append(name).append("\": {");
                for (GDPropertyMetadata meta : metadata) {
                    if (path.indexOf(Constants.PATH_DELIMITER) > 0) {
                        builder.append('"').append(meta.getName()).append('"').append(':')
                                .append(meta.getSerializedNode(path.substring(path.indexOf(Constants.PATH_DELIMITER)), format));
                    } else {
                        builder.append('"').append(meta.getName()).append('"').append(':')
                                .append(meta.getSerializedNode("", format));
                    }
                    builder.append(',');
                }
                builder.setLength(builder.length() - 1);
                builder.append('}');
                retVal = builder.toString();
            }
        } else {
            throw new GDException(405, "No such format supported");
        }

        return retVal;
    }

}
