package com.ericsson.common.util.serialization;

/**
 *
 * @author delma
 */
public enum Format {

    JSON, JSON_WDC, XML;

    public boolean isJson() {
        return this == JSON || this == JSON_WDC;
    }
}
