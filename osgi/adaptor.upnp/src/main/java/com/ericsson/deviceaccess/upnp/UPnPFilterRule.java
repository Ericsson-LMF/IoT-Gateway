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

import java.util.Objects;
import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPService;

/**
 * Filter
 * <p/>
 * UPnP Device Service Specification V1.1 ======================================
 * <p/>
 * 111.16.4 --------
 * <p/>
 * The valid subset of properties for the registration of UPnP Event Listener
 * services are:
 * <p/>
 * UPnPDevice.TYPE Which type of device to listen for events. UPnPDevice.ID The
 * ID of a specific device t listen for events. UPnPService.TYPE The type of a
 * specific service to listen for events. UPnPService.ID The ID of a specific
 * service to listen for events.
 */
// @author{Ryoji Kato}
class FR_DeviceType extends UPnPFilterRule {

    private String type = null;

    protected FR_DeviceType(String deviceType) {
        type = Objects.requireNonNull(deviceType, getClass().getName());
    }

    @Override
    public String toFilterRule() {
        return "(" + UPnPDevice.TYPE + "=" + type + ")";
    }
}

class FR_DeviceID extends UPnPFilterRule {

    private String id = null;

    protected FR_DeviceID(String deviceID) {
        this.id = Objects.requireNonNull(deviceID, getClass().getName());
    }

    @Override
    public String toFilterRule() {
        return "(" + UPnPDevice.ID + "=" + id + ")";
    }
}

class FR_ServiceType extends UPnPFilterRule {

    private String type = null;

    protected FR_ServiceType(String serviceType) {
        this.type = Objects.requireNonNull(serviceType, getClass().getName());
    }

    @Override
    public String toFilterRule() {
        return "(" + UPnPService.TYPE + "=" + type + ")";
    }
}

class FR_ServiceID extends UPnPFilterRule {

    private String id = null;

    protected FR_ServiceID(String serviceID) {
        this.id = Objects.requireNonNull(serviceID, getClass().getName());
    }

    @Override
    public String toFilterRule() {
        return "(" + UPnPService.ID + "=" + id + ")";
    }
}

class FR_and extends UPnPFilterRule {

    private UPnPFilterRule fr1 = null, fr2 = null;

    protected FR_and(UPnPFilterRule fr1, UPnPFilterRule fr2) {
        this.fr1 = Objects.requireNonNull(fr1, getClass().getName());
        this.fr2 = Objects.requireNonNull(fr2, getClass().getName());
    }

    @Override
    public String toFilterRule() {
        return "(&" + fr1.toFilterRule() + fr2.toFilterRule() + ")";
    }
}

class FR_or extends UPnPFilterRule {

    private UPnPFilterRule fr1 = null, fr2 = null;

    protected FR_or(UPnPFilterRule fr1, UPnPFilterRule fr2) {
        this.fr1 = Objects.requireNonNull(fr1, getClass().getName());
        this.fr2 = Objects.requireNonNull(fr2, getClass().getName());
    }

    @Override
    public String toFilterRule() {
        return "(|" + fr1.toFilterRule() + fr2.toFilterRule() + ")";
    }
}

class FR_not extends UPnPFilterRule {

    private UPnPFilterRule fr1 = null;

    protected FR_not(UPnPFilterRule fr1) {
        this.fr1 = Objects.requireNonNull(fr1, getClass().getName());
    }

    @Override
    public String toFilterRule() {
        return "(!" + fr1.toFilterRule() + ")";
    }
}

public abstract class UPnPFilterRule {

    static public UPnPFilterRule deviceType(String type) {
        return new FR_DeviceType(type);
    }

    static public UPnPFilterRule deviceID(String id) {
        return new FR_DeviceID(id);
    }

    static public UPnPFilterRule serviceType(String type) {
        return new FR_ServiceType(type);
    }

    static public UPnPFilterRule serviceID(String id) {
        return new FR_ServiceID(id);
    }

    static public UPnPFilterRule and(UPnPFilterRule fr1, UPnPFilterRule fr2) {
        return new FR_and(fr1, fr2);
    }

    static public UPnPFilterRule or(UPnPFilterRule fr1, UPnPFilterRule fr2) {
        return new FR_or(fr1, fr2);
    }

    static public UPnPFilterRule not(UPnPFilterRule fr1) {
        return new FR_not(fr1);
    }

    public abstract String toFilterRule();
}
