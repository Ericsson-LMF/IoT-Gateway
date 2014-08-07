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
package com.ericsson.deviceaccess.api.genericdevice;

import com.ericsson.common.util.serialization.View;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;

/**
 * Metadata about a property.
 */
public interface GDPropertyMetadata extends GDContextNode {

    String NAME = "name";
    String TYPE = "type";
    String MIN_VALUE = "minValue";
    String MAX_VALUE = "maxValue";
    String DEFAULT_VALUE = "defaultValue";

    /**
     * Gets the name of the property.
     *
     * @return the name of the property
     */
    @JsonView(View.ID.class)
    String getName();

    /**
     * Gets the type of the associated property.
     *
     * @return String.class, Integer.class or Float.class
     */
    @JsonIgnore
    Class getType();

    /**
     * Gets the type of the associated property.
     *
     * @return String.class, Integer.class or Float.class
     */
    @JsonProperty("type")
    String getTypeName();

    /**
     * Gets default value for this property.
     * <p/>
     * Use when it describes a string property.
     *
     * @return default value for this property when it describes a string
     * property
     */
    @JsonProperty("defaultValue")
    String getDefaultStringValue();

    /**
     * Gets the valid values for the associated property.
     * <p/>
     * Use when it describes a string property.
     *
     * @return the valid values for the associated property, or null if all
     * values are OK
     */
    String[] getValidValues();

    /**
     * Gets default value for this property.
     * <p/>
     * Use when it describes a number property.
     *
     * @return default value for this property when it describes a string
     * property
     */
    @JsonIgnore
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
