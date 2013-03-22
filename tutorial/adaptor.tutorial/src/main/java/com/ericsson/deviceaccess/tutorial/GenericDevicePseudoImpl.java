package com.ericsson.deviceaccess.tutorial;

import com.ericsson.deviceaccess.api.Constants;
import com.ericsson.deviceaccess.api.service.homeautomation.hvac.TemperatureSensor;

import com.ericsson.deviceaccess.api.service.homeautomation.hvac.thermostat.ThermostatMode;
import com.ericsson.deviceaccess.api.service.homeautomation.hvac.thermostat.ThermostatSetPoint;
import com.ericsson.deviceaccess.api.service.util.PowerControl;
import com.ericsson.deviceaccess.api.service.util.PowerMeter;
import com.ericsson.deviceaccess.spi.schema.SchemaBasedGenericDevice;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDevice;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDeviceUpdateListener;
import org.osgi.framework.ServiceRegistration;

import java.util.HashMap;
import java.util.Properties;

/**
 * Implementation of a GenericDevice to realize a pseudo device run in the OSGi
 * framework. It implements several pseudo services with actions to print some
 * messages upon requests. This class is instantiated when a PseudoDevice is
 * discovered by the tutorial basedriver and registered with the DeviceFactory.
 */
public class GenericDevicePseudoImpl extends SchemaBasedGenericDevice implements
		PseudoDeviceUpdateListener {
	private ServiceRegistration devReg;
	PseudoDevice pdev;

	public GenericDevicePseudoImpl(PseudoDevice device) {
		this.pdev = device;
		init();
	}

	void init() {
		pdev.setParameterUpdateListener(this);

		setURN(pdev.getURN());
		setId(pdev.getId());
		setOnline(true);
		setName("Device for tutorial");
		setType("power switch");
		setProtocol("pseudo");

		HashMap services = new HashMap();
		services.put(PowerControl.SERVICE_NAME, new PowerControlImpl(pdev));
		services.put(PowerMeter.SERVICE_NAME, new PowerMeterImpl(pdev));
		services.put(CustomService.SERVICE_NAME, new CustomService());
		services.put(AugmentedDimming.SERVICE_NAME, new AugmentedDimming());
		services.put(TemperatureSensor.SERVICE_NAME, new TempSensorImpl());
		services.put(ThermostatSetPoint.SERVICE_NAME,
				new ThermostatSetpointImpl());
		services.put(ThermostatMode.SERVICE_NAME, new ThermostatModeImpl());
		setService(services);
	}

	void destroy() {
		pdev.unsetParameterUpdateListener(this);
	}

	void setServiceRegistration(ServiceRegistration reg) {
		this.devReg = reg;
	}

	ServiceRegistration getServiceRegistration() {
		return devReg;
	}

	private void notifyUpdate(String path) {
		if (devReg != null) {
			Properties props = this.getDeviceProperties();
			props.setProperty(Constants.UPDATED_PATH, path);
			devReg.setProperties(props);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ericsson.deviceaccess.tutorial.pseudo.PseudoDeviceUpdateListener#
	 * pseudoDeviceUpdated(java.lang.String)
	 */
	public void pseudoDeviceUpdated(String data, boolean active) {
		PowerMeterImpl powerMeterService = (PowerMeterImpl) getService(PowerMeter.SERVICE_NAME);
		powerMeterService.setCurrentPower(Float.parseFloat(data));
		notifyUpdate(powerMeterService.getPath(true));
		CustomService customService = (CustomService) getService(CustomService.SERVICE_NAME);
		customService.update(active);
		TempSensorImpl temperatureSensor = (TempSensorImpl) getService(TemperatureSensor.SERVICE_NAME);
		ThermostatSetpointImpl thermostatSetpoint = (ThermostatSetpointImpl) getService(ThermostatSetPoint.SERVICE_NAME);
		temperatureSensor.updateTemp(thermostatSetpoint
				.getCurrentDesiredHeatingTemperature());
		notifyUpdate(customService.getPath(true));
	}
}
