/*
 * Copyright Ericsson AB 2011-2014. All Rights Reserved.
 * 
 * The contents of this file are subject to the Lesser GNU Public License,
 *  (the "License"), either version 2.1 of the License, or
 * (at your option) any later version.; you may not use this file except in
 * compliance with the License. You should have received a copy of the
 * License along with this software. If not, it can be
 * retrieved online at https://www.gnu.org/licenses/lgpl.html. Moreover
 * it could also be requested from Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 * 
 * BECAUSE THE LIBRARY IS LICENSED FREE OF CHARGE, THERE IS NO
 * WARRANTY FOR THE LIBRARY, TO THE EXTENT PERMITTED BY APPLICABLE LAW.
 * EXCEPT WHEN OTHERWISE STATED IN WRITING THE COPYRIGHT HOLDERS AND/OR
 * OTHER PARTIES PROVIDE THE LIBRARY "AS IS" WITHOUT WARRANTY OF ANY KIND,
 
 * EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE. THE ENTIRE RISK AS TO THE QUALITY AND PERFORMANCE OF THE
 * LIBRARY IS WITH YOU. SHOULD THE LIBRARY PROVE DEFECTIVE,
 * YOU ASSUME THE COST OF ALL NECESSARY SERVICING, REPAIR OR CORRECTION.
 *
 * IN NO EVENT UNLESS REQUIRED BY APPLICABLE LAW OR AGREED TO IN WRITING
 * WILL ANY COPYRIGHT HOLDER, OR ANY OTHER PARTY WHO MAY MODIFY AND/OR
 * REDISTRIBUTE THE LIBRARY AS PERMITTED ABOVE, BE LIABLE TO YOU FOR
 * DAMAGES, INCLUDING ANY GENERAL, SPECIAL, INCIDENTAL OR CONSEQUENTIAL
 * DAMAGES ARISING OUT OF THE USE OR INABILITY TO USE THE LIBRARY
 * (INCLUDING BUT NOT LIMITED TO LOSS OF DATA OR DATA BEING RENDERED
 * INACCURATE OR LOSSES SUSTAINED BY YOU OR THIRD PARTIES OR A FAILURE
 * OF THE LIBRARY TO OPERATE WITH ANY OTHER SOFTWARE), EVEN IF SUCH
 * HOLDER OR OTHER PARTY HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES. 
 * 
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
