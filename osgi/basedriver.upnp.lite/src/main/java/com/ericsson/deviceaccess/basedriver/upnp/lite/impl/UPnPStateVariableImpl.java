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
import java.util.Hashtable;

import org.osgi.service.upnp.UPnPStateVariable;

public class UPnPStateVariableImpl implements UPnPStateVariable {
	String name;
	String dataType;
	boolean sendEvents;
	String[] allowedValues;
	Number maximum;
	Number minimum;
	Number step;
	private String defaultValue;
	private String value = null;

	private static Hashtable dataTypeMap = null;
	static {
		dataTypeMap = new Hashtable(30);
		String[] upnpType = null;
		
		upnpType = new String[]{"ui1","ui2","i1","i2","i4","int"};
		for (int i = 0; i < upnpType.length; i++)
			dataTypeMap.put(upnpType[i],Integer.class);

		upnpType = new String[]{"ui4","time","time.tz"};
		for (int i = 0; i < upnpType.length; i++)
			dataTypeMap.put(upnpType[i],Long.class);
		
		upnpType = new String[]{"r4","float"};
		for (int i = 0; i < upnpType.length; i++)
			dataTypeMap.put(upnpType[i],Float.class);

		upnpType = new String[]{"r8","number","fixed.14.4"};
		for (int i = 0; i < upnpType.length; i++)
			dataTypeMap.put(upnpType[i],Double.class);

		upnpType = new String[]{"char"};
		for (int i = 0; i < upnpType.length; i++)
			dataTypeMap.put(upnpType[i],Character.class);

		upnpType = new String[]{"string","uri","uuid"};
		for (int i = 0; i < upnpType.length; i++)
			dataTypeMap.put(upnpType[i],String.class);

		upnpType = new String[]{"date","dateTime","dateTime.tz"};
		for (int i = 0; i < upnpType.length; i++)
			dataTypeMap.put(upnpType[i],Date.class);

		upnpType = new String[]{"boolean"};
		for (int i = 0; i < upnpType.length; i++)
			dataTypeMap.put(upnpType[i],Boolean.class);

		upnpType = new String[]{"bin.base64","bin.hex"};
		for (int i = 0; i < upnpType.length; i++)
			dataTypeMap.put(upnpType[i],byte[].class);
	}
	

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
	
	public String getName() {
		return name;
	}

	public Class getJavaDataType() {
		return (Class) dataTypeMap.get(dataType);
	}

	public String getUPnPDataType() {
		return dataType;
	}

	public Object getDefaultValue() {
		return defaultValue;
	}

	public String[] getAllowedValues() {
		return allowedValues;
	}

	public Number getMinimum() {
		return minimum;
	}

	public Number getMaximum() {
		return maximum;
	}

	public Number getStep() {
		return step;
	}

	public boolean sendsEvents() {
		return sendEvents;
	}

	public String toString() {
		StringBuffer ret = new StringBuffer("UPnPStateVariableImpl: ");
		
		ret.append("name=" + name);
		ret.append(", dataType=" + dataType);
		ret.append(", sendEvents=" + sendEvents);
		if (allowedValues == null)
			ret.append(", allowed=null");
		else {
			ret.append(", allowed=[");
			for (int i = 0; i < allowedValues.length; i++)
				ret.append(allowedValues[i] + ",");
			ret.append("]");
		}
		ret.append(", default=" + defaultValue);
		ret.append(", max=" + maximum);
		ret.append(", min=" + minimum);
		ret.append(", step=" + step);

		return ret.toString();
	}

	protected void setValue(String value) {
		this.value  = value;
	}
	
	protected String getValue() {
		return value;
	}
}
