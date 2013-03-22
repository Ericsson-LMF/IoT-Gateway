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

package com.ericsson.deviceaccess.api;

/**
 * Metadata about a property.
 */
public interface GenericDevicePropertyMetadata extends GenericDeviceContextNode {
	static final String NAME = "name";
	static final String TYPE = "type";
	static final String MIN_VALUE = "minValue";
	static final String MAX_VALUE = "maxValue";
	static final String DEFAULT_VALUE = "defaultValue";
	
    /**
     * Gets the name of the property.
     *
     * @return the name of the property
     */
    String getName();

    /**
     * Gets the type of the associated property.
     *
     * @return String.class, Integer.class or Float.class
     */
    Class getType();

    /**
     * Gets the type of the associated property.
     *
     * @return String.class, Integer.class or Float.class
     */
    String getTypeName();

    /**
     * Gets default value for this property.
     * <p/>
     * Use when it describes a string property.
     *
     * @return default value for this property when it describes a string
     *         property
     */
    String getDefaultStringValue();

    /**
     * Gets the valid values for the associated property.
     * <p/>
     * Use when it describes a string property.
     *
     * @return the valid values for the associated property, or null if all
     *         values are OK
     */
    String[] getValidValues();

    /**
     * Gets default value for this property.
     * <p/>
     * Use when it describes a number property.
     *
     * @return default value for this property when it describes a string
     *         property
     */
    Number getDefaultNumberValue();

    /**
     * Gets the min value for this property.
     * <p/>
     * Use when it describes a number property.
     *
     * @return the min value for this property
     */
    Number getMinValue();

    /**
     * Gets the max value for this property.
     * <p/>
     * Use when it describes a number property.
     *
     * @return the max value for this property
     */
    Number getMaxValue();

}
