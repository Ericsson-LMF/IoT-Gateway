package com.ericsson.commonutil.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 *
 * @author aopkarja
 */
public enum JsonFactory {

    /**
     * Singleton.
     */
    INSTANCE;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new Jdk7Module());
        MAPPER.registerModule(new ParameterNamesModule());
    }

    public static ObjectMapper get() {
        return MAPPER;
    }
}
