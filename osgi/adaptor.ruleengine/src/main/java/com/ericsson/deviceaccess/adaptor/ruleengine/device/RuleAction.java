package com.ericsson.deviceaccess.adaptor.ruleengine.device;

import java.util.HashMap;
import java.util.Iterator;

public class RuleAction {
	private String deviceId;
	private String serviceName;
	private String actionName;
	private HashMap arguments = new HashMap();
	
	public RuleAction() {
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	public HashMap getArguments() {
		return arguments;
	}

	public void setArguments(HashMap arguments) {
		this.arguments = arguments;
	}
	
	public String toString() {
		String res = deviceId + "." + serviceName + "." + actionName + "(";
		String sep = null;
		for (Iterator i = arguments.keySet().iterator(); i.hasNext(); ) {
			String key = (String) i.next();
			res += (sep==null ? sep="": ", ") + key + "=" + arguments.get(key);
		}
		res += ")";
		return res;
	}
}
