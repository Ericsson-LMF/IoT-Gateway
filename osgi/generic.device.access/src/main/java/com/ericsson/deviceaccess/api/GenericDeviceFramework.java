package com.ericsson.deviceaccess.api;

import java.util.List;

/**
 * This interface is intended to be used when using
 * GDA on non-OSGi platform, e.g. Android.
 * It mimics OSGi service framework interface without
 * dependency to OSGi specific API.
 *
 * @author ekenyas
 */
public interface GenericDeviceFramework {

    /**
     * This method should be called when a new device is discovered
     * and to be registered. It corresonds to
     *
     * @param dev The device to be registered
     * @throws GenericDeviceException
     */
    public void register(GenericDevice dev) throws GenericDeviceException;

    /**
     * This method should be called when an update occurs on the
     * concerning device, e.g. update on sensor reading.
     *
     * @param dev          The concerned device
     * @param updatedPaths Comma separated list of paths that the event concerns.
     * @throws GenericDeviceException
     */
    public void update(GenericDevice dev, String updatedPaths) throws GenericDeviceException;

    /**
     * Method to be called when the concerned device gets unavailable
     * and should be removed from the framework
     *
     * @param dev the device to be unregistered.
     * @throws GenericDeviceException
     */
    public void unregister(GenericDevice dev) throws GenericDeviceException;

    /**
     * Method to get the list of all the devices registered to the framework
     */
    List getAllDevices();

    /**
     * Method to get a device registered in the framework.
     *
     * @param id The ID of the device in question
     * @return GenericDevice instance that has the specified id. Returns null
     *         if no such device is found.
     */
    GenericDevice getDevice(String id);

    /**
     * Method to be called for adding a listener for device discovery events.
     *
     * @param listener
     */
    public void addListener(GenericDeviceFrameworkListener listener);

    /**
     * Method to be called when removing a listener for device discovery events.
     *
     * @param listener
     */
    public void removeListener(GenericDeviceFrameworkListener listener);


}
