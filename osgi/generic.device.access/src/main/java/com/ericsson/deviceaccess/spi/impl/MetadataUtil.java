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
import java.util.Iterator;
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
    public void verifyPropertiesAgainstMetadata(GDProperties props, Map metadataMap) throws GDError {
        if (metadataMap == null) {
            return;
        }
        for (String name : props.getNames()) {
            verifyPropertyAgainstMetadata(metadataMap, name, props.getValue(name));
        }
    }

    /**
     * Verifies the specified property, with the specified name, against the
     * specified metadata.
     *
     * @param metadataMap name:String -> {@link GDPropertyMetadata}
     * @param propertyName
     * @param propertyValue
     * @throws GDError thrown if value does not adhere to the metadata
     */
    public void verifyPropertyAgainstMetadata(Map metadataMap, String propertyName, Object propertyValue) throws GDError {
        GDPropertyMetadata metadata = (GDPropertyMetadata) metadataMap.get(propertyName);

        if (metadata == null) {
            return;
        }

        if (Number.class.isAssignableFrom(metadata.getType())) {
            if (propertyValue instanceof String) {
                try {
                    Float.parseFloat((String) propertyValue);
                } catch (NumberFormatException e) {
                    throw new GDError(
                            "The property: '" + propertyName + "'=" + propertyValue + " is a '" + propertyValue.getClass()
                            + "' which not parsable to '" + metadata.getType() + "'");
                }
            } else if (Float.class.isAssignableFrom(metadata.getType())) {
                if (!(propertyValue instanceof Float)) {
                    throw new GDError(
                            "The property: '" + propertyName + "'=" + propertyValue + " is a '" + propertyValue.getClass()
                            + "' which not assignable to '" + metadata.getType() + "'");
                }
            } else if (Long.class.isAssignableFrom(metadata.getType())) {
                if (!(propertyValue instanceof Long)) {
                    throw new GDError(
                            "The property: '" + propertyName + "'=" + propertyValue + " is a '" + propertyValue.getClass()
                            + "' which not assignable to '" + metadata.getType() + "'");
                }
            } else {
                if (!(propertyValue instanceof Integer || propertyValue instanceof Short || propertyValue instanceof Byte)) {
                    throw new GDError(
                            "The property: '" + propertyName + "'=" + propertyValue + " is a '" + propertyValue.getClass()
                            + "' which not assignable to '" + metadata.getType() + "'");
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
                            + "' is not among the allowed values '" + arrayToString(validValues) + "'");
                }
            }
        }
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
     * Makes a string of the specified array
     *
     * @param validValues
     * @return the array
     */
    private static String arrayToString(String[] validValues) {
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (String string : validValues) {
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
     * @throws GDException
     */
    public String metadataToJson(String path, Format format, String name, Collection metadata) throws GDException {
        String retVal = "";
        if (path == null) {
            path = "";
        }
        if (format.isJson()) {
            if (metadata.size() > 0) {
                StringBuilder sb = new StringBuilder();
                sb.append('"').append(name).append("\": {");
                for (Iterator iterator = metadata.iterator(); iterator.hasNext();) {
                    GDPropertyMetadata md = (GDPropertyMetadata) iterator.next();
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
            throw new GDException(405, "No such format supported");
        }

        return retVal;
    }
}
