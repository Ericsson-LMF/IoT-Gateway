package com.ericsson.deviceaccess.adaptor.ruleengine.device;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogReaderService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class ConfigurationManager implements ManagedService {
	private BundleContext context;
	private String pid;
    private Dictionary configProperties = new Properties(); 
	private ServiceRegistration sr;
	private Vector listeners = new Vector();
	
	public ConfigurationManager(BundleContext context, String pid) {
		this.context = context;
		this.pid = pid;
	}
	
	public interface ConfigurationManagerListener {
		public void updated(Dictionary added, Dictionary removed, Dictionary modified);
	}
	
	public void start() {
		Dictionary properties = new Properties();
		properties.put(Constants.SERVICE_PID, pid);
		sr = context.registerService(ManagedService.class.getName(), this, properties);
	}
	
	public void stop() {
		sr.unregister();
	}
	
	public void registerListener(ConfigurationManagerListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
	}
	
	public void unregisterListener(ConfigurationManagerListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public void setParameter(String key, String value) {
		configProperties.put(key, value);
		updateConfig(configProperties);
	}
	
	public void unsetParameter(String key) {
		if (configProperties.get(key) != null) {
			configProperties.remove(key);
			updateConfig(configProperties);
		}
	}

	public void updated(Dictionary properties) throws ConfigurationException {
		if (properties == null)
			return;
		
		properties.remove(Constants.SERVICE_PID);
		
		// Check for added configuration parameters
		Properties added = new Properties();
		for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			Object value = properties.get(key);
			if (configProperties.get(key) == null) {
				added.put(key, value);
				configProperties.put(key, value);
			}
		}
		
		// Check for removed configuration parameters
		Properties removed = new Properties();
		for (Enumeration e = configProperties.keys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			if (properties.get(key) == null) {
				removed.put(key, configProperties.get(key));
				configProperties.remove(key);
			}			
		}
		
		// Check for modified configuration parameters
		Properties modified = new Properties();
		for (Enumeration e = properties.keys(); e.hasMoreElements(); ) {
			String key = (String) e.nextElement();
			String newValue = (String) properties.get(key);
			String oldValue = (String) configProperties.get(key);
			if (! newValue.equals(oldValue)) {
				modified.put(key, newValue);
				configProperties.put(key, newValue);
			}
		}
		
		if (added.size() > 0 || removed.size() > 0 || modified.size() > 0) {
			synchronized (listeners) {
				for (Iterator i = listeners.iterator(); i.hasNext(); ) {
					ConfigurationManagerListener listener = (ConfigurationManagerListener) i.next();
					listener.updated(added, removed, modified);
				}
			}
		}
	}
	
	private void updateConfig(Dictionary updatedConfig) {
		// Assume contest holds a valid BundleContext object for the bundle
		ServiceReference ref = context.getServiceReference(ConfigurationAdmin.class.getName());
		if (ref != null) {
			ConfigurationAdmin cfgAdm = (ConfigurationAdmin) context.getService(ref);
			try {
				Configuration config = cfgAdm.getConfiguration(pid);
				config.update(updatedConfig);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			System.err.println("ConfigurationAdmin is not found");
		}
	}
}
