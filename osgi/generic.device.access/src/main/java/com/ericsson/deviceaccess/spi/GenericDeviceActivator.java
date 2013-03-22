/*
 * Copyright (c) Ericsson AB, 2013.
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

package com.ericsson.deviceaccess.spi;

import com.ericsson.deviceaccess.spi.event.EventManager;
import com.ericsson.research.common.slf4jlogger.OSGILogFactory;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

/**
 * GenericDeviceActivator is called when the GenericDevice bundle is activated
 * and deactivated by the OSGi framework.
 */
public class GenericDeviceActivator implements BundleActivator {

	private static EventManager eventManager;
	static {
		Bundle bundle = FrameworkUtil.getBundle(GenericDeviceActivator.class);
		eventManager = new EventManager();
	}

	{
		Bundle bundle = FrameworkUtil.getBundle(GenericDeviceActivator.class);
		OSGILogFactory.initOSGI(bundle != null ? bundle.getBundleContext()
				: null);
	}

	public void start(BundleContext context) throws Exception {
		eventManager.setContext(context);
		eventManager.start();
	}

	public void stop(BundleContext context) throws Exception {
		eventManager.shutdown();
	}

	public static EventManager getEventManager() {
		return eventManager;
	}
}
