/*
 * Copyright (c) Ericsson AB, 2011.
 *
 * All Rights Reserved. Reproduction in whole or in part is prohibited
 * without the written consent of the copyright owner.
 *
 * ERICSSON MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF
 * THE SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 * TO THE IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE, OR NON-INFRINGEMENT. ERICSSON SHALL NOT BE LIABLE FOR
 * ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR
 * DISTRIBUTING THIS SOFTWARE OR ITS DERIVATIVES.
 */

package com.ericsson.deviceaccess.tutorial;

import com.ericsson.deviceaccess.api.GenericDevice;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDevice;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDeviceDiscoveryListener;
import com.ericsson.deviceaccess.tutorial.pseudo.PseudoDeviceManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

import java.util.HashMap;
import java.util.Iterator;

/**
 * DeviceFactory class receives pseudo devices discovered by the tutorial
 * basedriver through the PseudoDeviceDiscoveryListener interface. This class is
 * started/stopped by the OSGi framework when the adaptor.tutorial bundle is
 * started/stopped.
 * 
 */
public class DeviceFactory implements BundleActivator,
		PseudoDeviceDiscoveryListener {
	private BundleContext context;
	private HashMap devices = new HashMap();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) {
		this.context = context;
		PseudoDeviceManager.getInstance().setDeviceDiscoveryListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ericsson.deviceaccess.tutorial.pseudo.PseudoDeviceDiscoveryListener
	 * #deviceDiscovered(com.ericsson.deviceaccess.tutorial.pseudo.PseudoDevice)
	 */
	public void deviceDiscovered(PseudoDevice dev) {
		if (!devices.containsKey(dev.getId())) {
			System.out.println("A new device is discovered!");
			GenericDevicePseudoImpl gdev = new GenericDevicePseudoImpl(dev);
			System.out.println("Created a GenericDevice object for "
					+ dev.getId());
			ServiceRegistration reg = context.registerService(
					GenericDevice.class.getName(), gdev,
					gdev.getDeviceProperties());
			System.out
					.println("Registered the GenericDevice object as OSGi service");
			gdev.setServiceRegistration(reg);
			devices.put(dev.getId(), gdev);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.ericsson.deviceaccess.tutorial.pseudo.PseudoDeviceDiscoveryListener
	 * #deviceRemoved(com.ericsson.deviceaccess.tutorial.pseudo.PseudoDevice)
	 */
	public void deviceRemoved(PseudoDevice dev) {
		if (devices.containsKey(dev.getId())) {
			System.out.println("Device " + dev.getId() + " is removed");
			GenericDevicePseudoImpl gdev = (GenericDevicePseudoImpl) devices
					.remove(dev.getId());
			ServiceRegistration reg = gdev.getServiceRegistration();
			if (reg != null) {
				gdev.setServiceRegistration(null);
				reg.unregister();
				System.out
						.println("Unregistered GenericDevice object from OSGi framework");
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) {
		Iterator it = devices.values().iterator();
		while (it.hasNext()) {
			GenericDevicePseudoImpl gdev = (GenericDevicePseudoImpl) it.next();
			ServiceRegistration reg = gdev.getServiceRegistration();
			if (reg != null) {
				gdev.setServiceRegistration(null);
				reg.unregister();
			}
			gdev.destroy();
		}
	}

}
