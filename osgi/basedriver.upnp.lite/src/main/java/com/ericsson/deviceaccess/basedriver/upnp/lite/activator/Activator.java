package com.ericsson.deviceaccess.basedriver.upnp.lite.activator;

import org.osgi.framework.*;
import com.ericsson.deviceaccess.basedriver.upnp.lite.impl.UPnPDeviceMgr;
import com.ericsson.research.common.slf4jlogger.OSGILogFactory;

public class Activator implements BundleActivator {
	private BundleContext context;
	private UPnPDeviceMgr upnpDeviceMgr;

	public void start(BundleContext context) throws Exception {
		this.context = context;
		
		OSGILogFactory.initOSGI(context);
		
        upnpDeviceMgr = new UPnPDeviceMgr(context);
        upnpDeviceMgr.start(context.getProperty("lan.ip"));
	}

	public void stop(BundleContext context) throws Exception {
		upnpDeviceMgr.stop();
	}
}
