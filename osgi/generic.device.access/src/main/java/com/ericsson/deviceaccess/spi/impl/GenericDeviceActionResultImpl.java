package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.GenericDeviceActionResult;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.api.Serializable;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;

public class GenericDeviceActionResultImpl implements GenericDeviceActionResult {
    private int code;
    private String reason;
    private GenericDeviceProperties value;

    /**
     * @param result
     */
    public GenericDeviceActionResultImpl(GenericDeviceProperties result) {
        value = result;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * {@inheritDoc}
     */
    public int getCode() {
        return code;
    }

    /**
     * {@inheritDoc}
     */
    public GenericDeviceProperties getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * {@inheritDoc}
     */
    public String getReason() {
        return reason;
    }

    //@Override
	public String serialize(int format) throws GenericDeviceException {
		GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (format == Serializable.FORMAT_JSON
                || format == Serializable.FORMAT_JSON_WDC) {
            StringBuffer sb = new StringBuffer("{");
            sb.append("\"code\":\"").append(getCode()).append("\",");
            sb.append("\"reason\":\"").append(getReason()).append("\",");
            if(value != null){
            	sb.append("\"value\":").append(getValue().serialize(format));
            } else {
            	sb.append("\"value\":null");
            }
            sb.append("}");
            
            return sb.toString();
        } else {
            throw new GenericDeviceException(405, "No such format supported");
        }	
	}
	

}
