package com.ericsson.deviceaccess.upnp;

import org.osgi.service.upnp.UPnPDevice;
import org.osgi.service.upnp.UPnPService;

/**
 * Filter
 * <p/>
 * UPnP Device Service Specification V1.1
 * ======================================
 * <p/>
 * 111.16.4
 * --------
 * <p/>
 * The valid subset of properties for the registration of
 * UPnP Event Listener services are:
 * <p/>
 * UPnPDevice.TYPE   Which type of device to listen for events.
 * UPnPDevice.ID     The ID of a specific device t listen for events.
 * UPnPService.TYPE  The type of a specific service to listen for events.
 * UPnPService.ID    The ID of a specific service to listen for events.
 */

// @author{Ryoji Kato}

class FR_DeviceType extends UPnPFilterRule {
    private String type = null;

    protected FR_DeviceType(String deviceType) {
        if (deviceType == null) {
            throw new NullPointerException("FR_DeviceType()");
        }
        this.type = deviceType;
    }

    public String toFilterRule() {
        return "(" + UPnPDevice.TYPE + "=" + type + ")";
    }
}

class FR_DeviceID extends UPnPFilterRule {
    private String id = null;

    protected FR_DeviceID(String deviceID) {
        if (deviceID == null) {
            throw new NullPointerException("FR_DeviceID()");
        }
        this.id = deviceID;
    }

    public String toFilterRule() {
        return "(" + UPnPDevice.ID + "=" + id + ")";
    }
}

class FR_ServiceType extends UPnPFilterRule {
    private String type = null;

    protected FR_ServiceType(String serviceType) {
        if (serviceType == null) {
            throw new NullPointerException("FR_ServiceType()");
        }
        this.type = serviceType;
    }

    public String toFilterRule() {
        return "(" + UPnPService.TYPE + "=" + type + ")";
    }
}

class FR_ServiceID extends UPnPFilterRule {
    private String id = null;

    protected FR_ServiceID(String serviceID) {
        if (serviceID == null) {
            throw new NullPointerException("FR_ServiceID()");
        }
        this.id = serviceID;
    }

    public String toFilterRule() {
        return "(" + UPnPService.ID + "=" + id + ")";
    }
}

class FR_and extends UPnPFilterRule {
    private UPnPFilterRule fr1 = null, fr2 = null;

    protected FR_and(
            UPnPFilterRule fr1,
            UPnPFilterRule fr2
    ) {
        if ((fr1 == null) || (fr2 == null)) {
            throw new NullPointerException("FR_and()");
        }
        this.fr1 = fr1;
        this.fr2 = fr2;
    }

    public String toFilterRule() {
        return "(&" + fr1.toFilterRule() + fr2.toFilterRule() + ")";
    }
}

class FR_or extends UPnPFilterRule {
    private UPnPFilterRule fr1 = null, fr2 = null;

    protected FR_or(
            UPnPFilterRule fr1,
            UPnPFilterRule fr2
    ) {
        if ((fr1 == null) || (fr2 == null)) {
            throw new NullPointerException("FR_or()");
        }
        this.fr1 = fr1;
        this.fr2 = fr2;
    }

    public String toFilterRule() {
        return "(|" + fr1.toFilterRule() + fr2.toFilterRule() + ")";
    }
}

class FR_not extends UPnPFilterRule {
    private UPnPFilterRule fr1 = null;
    ;

    protected FR_not(
            UPnPFilterRule fr1
    ) {
        if (fr1 == null) {
            throw new NullPointerException("FR_not()");
        }
        this.fr1 = fr1;
    }

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

    static public UPnPFilterRule and(
            UPnPFilterRule fr1,
            UPnPFilterRule fr2
    ) {
        return new FR_and(fr1, fr2);
    }

    static public UPnPFilterRule or(
            UPnPFilterRule fr1,
            UPnPFilterRule fr2
    ) {
        return new FR_or(fr1, fr2);
    }

    static public UPnPFilterRule not(
            UPnPFilterRule fr1
    ) {
        return new FR_not(fr1);
    }

    public abstract String toFilterRule();
}
