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

import com.ericsson.deviceaccess.api.GenericDeviceException;
import com.ericsson.deviceaccess.api.GenericDeviceProperties;
import com.ericsson.deviceaccess.spi.service.homeautomation.lighting.DimmingBase;
import java.util.Properties;
import org.osgi.service.upnp.UPnPAction;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPException;
import org.osgi.service.upnp.UPnPService;
import org.slf4j.Logger;

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

        UPnPAction action;
        try {
            action = DimmingUPnPImpl.getUPnPAction(upnpDev, "SetTarget");
        } catch (UPnPException e) {
            action = null;
        }
        this.switchPower_setTarget = action;

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
                logger.debug("updateCurrentLoadLevel(" + (Integer) value + ")");
                this.updateCurrentLoadLevel((Integer) value);
            }
        } else {
            // NOP
        }
    }
}
