package com.ericsson.deviceaccess.upnp;

import java.util.Properties;

import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.spi.service.homeautomation.power.SwitchPowerBase;

public class SwitchPowerUPnPImpl extends SwitchPowerBase implements UPnPDeviceAgent.UpdatePropertyInterface {

	final private UPnPDevice upnpDev;
	final private Logger logger;

	public SwitchPowerUPnPImpl(UPnPDevice upnpDev, UPnPService upnpService, Logger logger) {
		this.upnpDev = upnpDev;
		this.logger = logger;
	}
	
	// @Override
	public void executeSetTarget(int target) throws GenericDeviceException {
		// TODO Auto-generated method stub
		UPnPAction action = null;
		try {
			action = SwitchPowerUPnPImpl.getUPnPAction(this.upnpDev, "SetTarget");
			Properties args = new Properties();
			if (target == 0) {
				args.put("newTargetValue", "False");
			} else {
				args.put("newTargetValue", "True");				
			}
			action.invoke(args);
		} catch (UPnPException e) {
			e.printStackTrace();
			return;
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}		
	}

	// @Override
	protected void refreshProperties() {
		// TODO Auto-generated method stub
		
	}

	// @Override
	public void updateProperty(String name, Object value) {
		logger.debug("updateProperty(" + name + ")");
		
		GenericDeviceProperties properties = this.getProperties();
		if ("Status".equalsIgnoreCase(name)) {
			if (value instanceof Boolean) {
				logger.debug("updateCurrentTarget(" + (Boolean)value + ")");
				this.updateCurrentTarget(((Boolean)value).booleanValue() ? 1 : 0);
			}
		} else {
			// NOP
		}		
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
}
