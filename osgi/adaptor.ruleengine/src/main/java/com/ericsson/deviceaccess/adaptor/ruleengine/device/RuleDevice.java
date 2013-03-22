package com.ericsson.deviceaccess.adaptor.ruleengine.device;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.spi.schema.SchemaBasedGenericDevice;

public class RuleDevice extends SchemaBasedGenericDevice {
	private BundleContext context;
	private ServiceRegistration sr;
	private ConfigurationManager cm;
	private RuleService ruleService;
	private PropertyManager propertyManager;

	public RuleDevice(BundleContext context, ConfigurationManager cm) {
        this.context = context;
        this.cm = cm;

        setId("RuleEngine");
        setURN("RuleEngine");
        setOnline(true);
        setName("RuleEngine");
        setType("general");
        setProtocol("general");

		ruleService = new RuleService();
		putService(ruleService);
		
		propertyManager = new PropertyManager();
	}

	public void start() {
		ruleService.start(context, cm);
		propertyManager.start(context, ruleService);
		sr = context.registerService(GenericDevice.class.getName(), this, getDeviceProperties());
	}
	
	public void stop() {
		sr.unregister();
		ruleService.stop();
		propertyManager.stop();
	}
}
