package com.ericsson.deviceaccess.tutorial.pseudo;

public class PseudoDevice {
    int watt = 0;
    boolean active = true;

    public String getId() {
        return "tutorial";
    }

    public String getURN() {
    	return "eeb60e00-2724-11e1-bfc2-0800200c9a66";
    }
    
    public void powerOn() throws PseudoDeviceException {
        System.out.println("Turned ON!");
        active = true;
    }

    public void powerOff() throws PseudoDeviceException {
        System.out.println("Turned OFF!");
        active = false;
    }

    public String getConsumedPowerInWatt() throws PseudoDeviceException {
        return String.valueOf(watt);
    }

    public void setParameterUpdateListener(final PseudoDeviceUpdateListener listener) {
        new Thread(new Runnable() {
            /*
                * Parameter update emulation code.
                * It emulates a parameter update per 3 seconds
                */
            public void run() {
                while (listener != null) {
                    try {
                        Thread.sleep(60*1000);
                        watt++;
                        listener.pseudoDeviceUpdated(getConsumedPowerInWatt(), active);
                        active = !active;
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (PseudoDeviceException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }

        }).start();
    }

    public void unsetParameterUpdateListener(
            PseudoDeviceUpdateListener listener) {
        listener = null;

    }
}
