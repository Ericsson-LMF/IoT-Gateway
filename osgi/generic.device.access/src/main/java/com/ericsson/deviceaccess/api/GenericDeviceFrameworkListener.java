package com.ericsson.deviceaccess.api;


/**
 * This interface is intended to be used when using
 * GDA on non-OSGi platform, e.g. Android.
 * It mimics OSGi service tracker interface without
 * dependency to OSGi specific API.
 *
 * @author ekenyas
 */
public interface GenericDeviceFrameworkListener {

    /**
     * Invoked when a new device is discovered.
     *
     * @param dev a newly discovered device.
     * @return true if the listener wants to be posted on the events occured on the device, e.g. update, unregister.
     * @throws GenericDeviceFrameworkException
     *
     */
    public boolean addingDevice(GenericDevice dev) throws GenericDeviceFrameworkException;

    /**
     * Invoked when a parameter update occurs on the device.
     *
     * @param dev          the concerned device.
     * @param updatedPaths comma-separated list of paths that the event concerns.
     * @throws GenericDeviceFrameworkException
     *
     */
    public void modifiedDevice(GenericDevice dev, String updatedPaths) throws GenericDeviceFrameworkException;

    /**
     * Invoked when a device is removed.
     *
     * @param dev A device removed from the framework.
     * @throws GenericDeviceFrameworkException
     *
     */
    public void removedDevice(GenericDevice dev) throws GenericDeviceFrameworkException;
}
