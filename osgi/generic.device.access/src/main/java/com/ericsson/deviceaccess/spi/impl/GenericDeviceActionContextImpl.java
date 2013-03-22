package com.ericsson.deviceaccess.spi.impl;

import com.ericsson.deviceaccess.api.GenericDeviceActionContext;
import com.ericsson.deviceaccess.api.GenericDeviceActionResult;
import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.api.Serializable;
import com.ericsson.deviceaccess.spi.GenericDeviceAccessSecurity;


public class GenericDeviceActionContextImpl extends GenericDeviceActionContext.Stub implements GenericDeviceActionContext {
    private String requester;
    private String requesterContact;
    private String owner;
    private String device;
    private String service;
    private String action;
    private boolean isAuthorized = true;
    private boolean isFirstTime = true;
    private boolean isExecuted = false;
    private boolean isFailed = false;
    private long messageThreadId = this.hashCode();
    private GenericDeviceProperties arguments;
    private GenericDeviceActionResult result;

    /**
     * @param arguments
     * @param result
     */
    public GenericDeviceActionContextImpl(GenericDeviceProperties arguments, GenericDeviceProperties result) {
        super();
        this.arguments = arguments;
        this.result = new GenericDeviceActionResultImpl(result);
    }


    public void setRequester(String requester) {
        this.requester = requester;
    }

    public String getRequester() {
        return requester;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDevice() {
        return device;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getAction() {
        return action;
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public boolean isAuthorized() {
        if (getRequester() != null && getRequester().equals(getOwner())) {
            return true;
        } else {
            return this.isAuthorized;
        }
    }


    public void setFirstTime(boolean isFirstTime) {
        this.isFirstTime = isFirstTime;
    }

    public boolean isFirstTime() {
        return isFirstTime;
    }


    public long getMessageThreadId() {
        return messageThreadId;
    }

    public void setMessageThreadId(long id) {
        this.messageThreadId = id;
    }

    public void setExecuted(boolean isExecuted) {
        this.isExecuted = isExecuted;
    }

    public boolean isExecuted() {
        return isExecuted;
    }

    public void setFailed(boolean isFailed) {
        this.isFailed = isFailed;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public GenericDeviceActionResult getResult() {
        return result;
    }

    public void setRequesterContact(String requesterContact) {
        this.requesterContact = requesterContact;
    }

    public String getRequesterContact() {
        return requesterContact;
    }

    public GenericDeviceProperties getArguments() {
        return arguments;
    }


	public void setResult(GenericDeviceActionResult result) {
		this.result = result;		
	}


	public String serialize(int format) throws GenericDeviceException {
		GenericDeviceAccessSecurity.checkGetPermission(getClass().getName());
        if (format == Serializable.FORMAT_JSON
                || format == Serializable.FORMAT_JSON_WDC) {
            StringBuffer sb = new StringBuffer("{");
            sb.append("\"device\":\"").append(getDevice()).append("\",");
            sb.append("\"service\":\"").append(getService()).append("\",");
            sb.append("\"action\":\"").append(getAction()).append("\",");
            sb.append("\"requester\":\"").append(getRequester()).append("\",");
            sb.append("\"owner\":\"").append(getOwner()).append("\",");
            sb.append("\"firstTime\":\"").append(isFirstTime()).append("\",");
            sb.append("\"authorized\":\"").append(isAuthorized()).append("\",");
            sb.append("\"requesterContact\":\"").append(getRequesterContact()).append("\",");
            if(arguments != null){
            	sb.append("\"arguments\":").append(getArguments().serialize(format)).append(",");
            } else {
            	sb.append("\"arguments\":null,");
            }
            if(result != null){
            	sb.append("\"result\":").append(getResult().serialize(format));
            } else {
            	sb.append("\"result\":null");
            }
            sb.append("}");
            
            return sb.toString();
        } 
        throw new GenericDeviceException(405, "No such format supported");
	}


}
