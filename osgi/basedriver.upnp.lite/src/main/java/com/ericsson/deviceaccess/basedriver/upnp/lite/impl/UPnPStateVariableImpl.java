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
