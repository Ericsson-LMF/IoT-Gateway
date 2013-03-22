package com.ericsson.deviceaccess.upnp;

public class UPnPUtilException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 42079230325239414L;
    int errorCode;

    public UPnPUtilException(int code, String msg) {
        super(msg);
        errorCode = code;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
