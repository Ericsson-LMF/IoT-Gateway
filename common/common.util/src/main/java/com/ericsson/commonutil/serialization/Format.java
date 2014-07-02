package com.ericsson.commonutil.serialization;

/**
 *
 * @author aopkarja
 */
public enum Format {

    JSON, JSON_WDC, XML;

    public boolean isJson() {
        return this == JSON || this == JSON_WDC;
    }
}
