package com.ericsson.deviceaccess.coap.basedriver.communication;

/**
 *
 * @author delma
 */
public enum UDPConstants {

    /**
     * Singleton.
     */
    INSTANCE;

    // IP MTU is suggested to 1280 bytes in draft-ieft-core-coap-08, 1152 for
    // the message size, 1024 for the payload size
    public static final int MAX_DATAGRAM_SIZE = 1280;
}
