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
package com.ericsson.deviceaccess.upnp;

import com.ericsson.research.common.slf4jlogger.OSGILogFactory;
import java.util.HashMap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UPnPAgentFactory implements BundleActivator, ServiceTrackerCustomizer {

    private static final Logger logger = LoggerFactory.getLogger(UPnPAgentFactory.class);

    private BundleContext context;
    private ServiceTracker upnpDevTracker;
    private HashMap agents = new HashMap();

    @Override
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
        }
    }

    @Override
    public void stop(BundleContext context) {
        upnpDevTracker.close();
        upnpDevTracker = null;
    }

    private void addGDADevice(UPnPDevice devOSGi) {
        logger.warn("Creating agent for " + UPnPUtil.getFriendlyName(devOSGi));
        UPnPDeviceAgent agent = new UPnPDeviceAgent(context, devOSGi);

        agents.put(devOSGi, agent);
        agent.start();
    }

    @Override
    public Object addingService(ServiceReference ref) {
        //if (ref.getProperty(UPnPDevice.UPNP_EXPORT) != null) {
        //logger.debug("This device is created locally on this gateway. Will skip");
        //return null;
        //}
        UPnPDevice dev = (UPnPDevice) context.getService(ref);
        addGDADevice(dev);

        return dev;
    }

    @Override
    public void modifiedService(ServiceReference ref, Object object) {
        UPnPDevice dev = (UPnPDevice) object;
        UPnPDeviceAgent agent = (UPnPDeviceAgent) agents.get(dev);
        if (agent != null) {
            agent.update();
        } else {
            logger.error("Agent was not found although service is modified (not added)");
        }
    }

    @Override
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
