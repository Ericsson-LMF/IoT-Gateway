package com.ericsson.deviceaccess.upnp;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.research.common.slf4jlogger.OSGILogFactory;

import java.util.HashMap;

public class UPnPAgentFactory implements BundleActivator, ServiceTrackerCustomizer {
    private BundleContext context;
    private ServiceTracker upnpDevTracker;
    private HashMap agents = new HashMap();
    private static final Logger logger = LoggerFactory.getLogger(UPnPAgentFactory.class);

	public void start(BundleContext context) {
        OSGILogFactory.initOSGI(context);
        this.context = context;

        logger.debug("Starting UPnP agent factory");
        upnpDevTracker = new ServiceTracker(context, UPnPDevice.class.getName(), this);
        upnpDevTracker.open();
        
        /*
           * Workaround in case System property file.encoding is not set by frameowork configuration
           * 20101122 Kenta
           */
        if (!"UTF-8".equals(System.getProperty("file.encoding"))) {
            try {
                System.setProperty("file.encoding", "UTF-8");
            } catch (SecurityException e) {
                logger.warn("Could not set file.encoding system property by myself.");
            }
            ;
        }
    }

    public void stop(BundleContext context) {
        upnpDevTracker.close();
        upnpDevTracker = null;
    }
    
    private void addGDADevice(UPnPDevice devOSGi) {
        logger.debug("Creating agent for " + UPnPUtil.getFriendlyName(devOSGi));
        UPnPDeviceAgent agent = new UPnPDeviceAgent(context, devOSGi);

        agents.put(devOSGi, agent);
        agent.start();
    }


    public Object addingService(ServiceReference ref) {
        //if (ref.getProperty(UPnPDevice.UPNP_EXPORT) != null) {
            //logger.debug("This device is created locally on this gateway. Will skip");
            //return null;
        //}
        UPnPDevice dev = (UPnPDevice) context.getService(ref);
        addGDADevice(dev);
        
        return dev;
    }

    public void modifiedService(ServiceReference ref, Object object) {
        UPnPDevice dev = (UPnPDevice) object;
        UPnPDeviceAgent agent = (UPnPDeviceAgent) agents.get(dev);
        if (agent != null) {
            agent.update();
        } else {
            logger.error("Agent was not found although service is modified (not added)");
        }
    }

    public void removedService(ServiceReference ref, Object object) {
        UPnPDevice dev = (UPnPDevice) object;
        UPnPDeviceAgent agent = (UPnPDeviceAgent) agents.get(dev);
        if (agent != null) {
            agent.stop();
            agents.remove(dev);
        } else {
            logger.error("Agent was not found although service is removed ");
        }
    }
}
