package com.ericsson.deviceaccess.adaptor.ruleengine;

import org.osgi.framework.*;

import com.ericsson.deviceaccess.adaptor.ruleengine.device.ConfigurationManager;
import com.ericsson.deviceaccess.adaptor.ruleengine.device.RuleDevice;

public class Activator implements BundleActivator {
	private static final String PID = "com.ericsson.deviceaccess.adapter.ruleengine";
	private ConfigurationManager cm;
	private RuleDevice ruleDevice;
	public static BundleContext context;

	public void start(BundleContext bc) throws Exception {
		this.context = bc;
		cm = new ConfigurationManager(bc, PID);
		ruleDevice = new RuleDevice(bc, cm);
		ruleDevice.start();
		cm.start();
	}

	public void stop(BundleContext bc) throws Exception {
		ruleDevice.stop();
		cm.stop();
	}
}
