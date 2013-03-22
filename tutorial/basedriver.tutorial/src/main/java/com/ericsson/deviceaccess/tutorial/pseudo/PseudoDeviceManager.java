package com.ericsson.deviceaccess.tutorial.pseudo;


public class PseudoDeviceManager {
    private static PseudoDeviceManager self;

    private PseudoDeviceManager() {
    }

    public static PseudoDeviceManager getInstance() {
        if (self == null) {
            self = new PseudoDeviceManager();
        }
        return self;
    }

    public void setDeviceDiscoveryListener(final PseudoDeviceDiscoveryListener listener) {
        new Thread(new Runnable() {
            /*
                * Device discovery emulation code.
                * It emulates a device is discovered 2 seconds after the bundle is started,
                * and removed 20 seconds after that.
                */

            public void run() {
                try {
                    Thread.sleep(2000);

                    PseudoDevice dev = new PseudoDevice();
                    listener.deviceDiscovered(dev);

                    Thread.sleep(10*60*1000);

                    listener.deviceRemoved(dev);
                    Thread.sleep(3000);

                    listener.deviceDiscovered(dev);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }).start();


    }
}
