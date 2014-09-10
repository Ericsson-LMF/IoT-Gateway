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
package com.ericsson.deviceaccess.basedriver.upnp.lite.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.osgi.service.upnp.UPnPStateVariable;

public class UPnPStateVariableImpl implements UPnPStateVariable {

    private static final Map<String, Class<?>> dataTypeMap = new HashMap<>(30);

    static {
        mapType(Integer.class, "ui1", "ui2", "i1", "i2", "i4", "int");
        mapType(Long.class, "ui4", "time", "time.tz");
        mapType(Float.class, "r4", "float");
        mapType(Double.class, "r8", "number", "fixed.14.4");
        mapType(Character.class, "char");
        mapType(String.class, "string", "uri", "uuid");
        mapType(Date.class, "date", "dateTime", "dateTime.tz");
        mapType(Boolean.class, "boolean");
        mapType(byte[].class, "bin.base64", "bin.hex");
    }

    private static void mapType(Class<?> value, String... keys) {
        for (String key : keys) {
            dataTypeMap.put(key, value);
        }
    }

    String name;
    String dataType;
    boolean sendEvents;
    String[] allowedValues;
    Number maximum;
    Number minimum;
    Number step;
    private final String defaultValue;
    private String value = null;

    public UPnPStateVariableImpl(String name, String dataType, boolean sendEvents, String[] allowedValues, Number maximum, Number minimum, Number step, String defaultValue) {
        this.name = name;
        this.dataType = dataType;
        this.sendEvents = sendEvents;
        this.allowedValues = allowedValues;
        this.maximum = maximum;
        this.minimum = minimum;
        this.step = step;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Class getJavaDataType() {
        return dataTypeMap.get(dataType);
    }

    @Override
    public String getUPnPDataType() {
        return dataType;
    }

    @Override
    public Object getDefaultValue() {
        return defaultValue;
    }

    @Override
    public String[] getAllowedValues() {
        return allowedValues;
    }

    @Override
    public Number getMinimum() {
        return minimum;
    }

    @Override
    public Number getMaximum() {
        return maximum;
    }

    @Override
    public Number getStep() {
        return step;
    }

    @Override
    public boolean sendsEvents() {
        return sendEvents;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("UPnPStateVariableImpl: ");
        builder.append("name=").append(name);
        builder.append(", dataType=").append(dataType);
        builder.append(", sendEvents=").append(sendEvents);
        if (allowedValues == null) {
            builder.append(", allowed=null");
        } else {
            builder.append(", allowed=[");
            for (String allowedValue : allowedValues) {
                builder.append(allowedValue).append(",");
            }
            builder.append("]");
        }
        builder.append(", default=").append(defaultValue);
        builder.append(", max=").append(maximum);
        builder.append(", min=").append(minimum);
        builder.append(", step=").append(step);
        return builder.toString();
    }

    protected void setValue(String value) {
        this.value = value;
    }

    protected String getValue() {
        return value;
    }

}
