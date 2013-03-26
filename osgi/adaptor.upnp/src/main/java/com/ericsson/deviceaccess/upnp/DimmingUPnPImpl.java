package com.ericsson.deviceaccess.upnp;

import java.util.Dictionary;
import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Filter;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPEventListener;
import org.osgi.service.upnp.UPnPException;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.spi.service.homeautomation.lighting.DimmingBase;

/**
 * Dimming GDA for UPnP Dimmable Light
 * 
 * http://upnp.org/specs/ha/UPnP-ha-DimmableLight-v1-Device.pdf
 * 
 * @author Ryoji Kato <ryoji.kato@ericsson.com>
 *
 */
public class DimmingUPnPImpl extends DimmingBase implements UPnPDeviceAgent.UpdatePropertyInterface {

	final private UPnPAction switchPower_setTarget;
	final private UPnPAction dimmingService_setLoadLevelTarget;
	final private Logger logger;
	
	public DimmingUPnPImpl(UPnPDevice upnpDev, UPnPService upnpService, Logger logger) {
		logger.debug("DimmingUPnPImpl()");
		
		this.logger = logger;
		
		UPnPAction action = null;
		try {
			action = DimmingUPnPImpl.getUPnPAction(upnpDev, "SetTarget");
		} catch (UPnPException e) {
			action = null;
		}
		this.switchPower_setTarget = action;
		
		action = null;
		try {
			action = DimmingUPnPImpl.getUPnPAction(upnpDev, "SetLoadLevelTarget");
		} catch (UPnPException e) {
			action = null;
		}
		this.dimmingService_setLoadLevelTarget = action;
	}
	
	//@Override
	public void executeOff() throws GenericDeviceException {
		this.logger.debug("DimmingUPnPImpl::executeOff()");
		
		Properties args = new Properties();
		args.put("newTargetValue", "False");
		try {
			this.switchPower_setTarget.invoke(args);
			this.updateCurrentLoadLevel(0);
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}

	//@Override
	public void executeOn() throws GenericDeviceException {
		this.logger.debug("DimmingUPnPImpl::executeOn()");
		
		Properties args = new Properties();
		args.put("newTargetValue", "True");
		try {
			this.switchPower_setTarget.invoke(args);
			this.updateCurrentLoadLevel(100);
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}

	//@Override
	public void executeSetLoadLevelTarget(int lvl)
			throws GenericDeviceException {
		this.logger.debug("DimmingUPnPImpl::executeSetLoadLevelTarget(" + lvl + ")");		

		Properties args = new Properties();
		args.put("NewLoadLevelTarget", Integer.toString(lvl));
		try {
			this.dimmingService_setLoadLevelTarget.invoke(args);
			this.updateCurrentLoadLevel(lvl);
		} catch (Exception e) {
			e.printStackTrace();
		}				
		
	}

	//@Override
	public void executeSetLoadLevelTargetWithRate(int lvl, float rate)
			throws GenericDeviceException {
		this.logger.debug("DimmingUPnPImpl::executeSetLoadLevelTargetWithRate(" + lvl + "," + rate + ")");		
		
		this.executeSetLoadLevelTarget(lvl);
	}

	//@Override
	protected void refreshProperties() {
		this.logger.debug("DimmingUPnPImpl::refreshProperties()");		
	}

	
    private static UPnPAction getUPnPAction(UPnPDevice device, String actionName)
            throws UPnPException {
        UPnPService[] services = device.getServices();

        for (int i = 0; i < services.length; ++i) {
            UPnPAction action = services[i].getAction(actionName);
            if (action != null) {
                return action;
            }
        }
        throw new UPnPException(UPnPException.INVALID_ACTION,
                "No such action supported " + actionName);
    }

	// @Override
	public void updateProperty(String name, Object value) {
		logger.debug("updateProperty(" + name + ")");
		
		GenericDeviceProperties properties = this.getProperties();
		if ("LoadLevelStatus".equalsIgnoreCase(name)) {
			if (value instanceof Integer) {
				logger.debug("updateCurrentLoadLevel(" + (Integer)value + ")");
				this.updateCurrentLoadLevel(((Integer)value).intValue());
			}
		} else {
			// NOP
		}
	}
}