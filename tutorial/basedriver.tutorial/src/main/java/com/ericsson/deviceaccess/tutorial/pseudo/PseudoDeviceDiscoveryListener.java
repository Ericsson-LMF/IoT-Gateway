package com.ericsson.deviceaccess.tutorial.pseudo;

public interface PseudoDeviceDiscoveryListener {

    public void deviceDiscovered(PseudoDevice dev);

    public void deviceRemoved(PseudoDevice dev);
}
