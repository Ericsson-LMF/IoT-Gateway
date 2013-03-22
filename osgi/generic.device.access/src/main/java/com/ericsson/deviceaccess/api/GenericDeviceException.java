package com.ericsson.deviceaccess.api;


public class GenericDeviceException extends Exception {

    /**
     *
     */
    private static final long serialVersionUID = -1611830596441206137L;
    private int code;

    public GenericDeviceException(int code, String msg, Exception e) {
        super(msg, e);
        this.code = code;
    }

    public GenericDeviceException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public GenericDeviceException(String msg, Exception e) {
        this(500, msg, e);
    }
    
    public GenericDeviceException(String msg) {
        this(500, msg);
    }

    public int getCode() {
    	return code;
    }
}
