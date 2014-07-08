package com.ericsson.commonutil.serialization;

/**
 * Exception that happens while serialization
 *
 * @author aopkarja
 */
public class SerializationException extends Exception {

    public SerializationException(String message) {
        super(message);
    }

    public SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

}
