package com.ericsson.common.util.serialization;

/**
 * Exception that happens while serialization
 *
 * @author delma
 */
public class SerializationException extends Exception {

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
