package com.ericsson.commonutil.serialization;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;

/**
 *
 * @author aopkarja
 */
public enum SerializationUtil {

    /**
     * Singleton.
     */
    INSTANCE;

    private static final JacksonXmlModule XML_MODULE = new JacksonXmlModule();

    static {
        //XML configuration
    }
    private static final ObjectMapper XML_MAPPER = new XmlMapper(XML_MODULE);

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    static {
        JSON_MAPPER.registerModule(new Jdk7Module());
        JSON_MAPPER.registerModule(new ParameterNamesModule());
        JSON_MAPPER.registerModule(new MrBeanModule());

        JSON_MAPPER.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        JSON_MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        JSON_MAPPER.setSerializationInclusion(Include.NON_EMPTY);
    }

    public static ObjectMapper get(Format format) {
        if (format.isJson()) {
            return JSON_MAPPER;
        }
        return XML_MAPPER;
    }

    /**
     * Reduces how many interfaces Ignores have to implement for Views without
     * Ignore
     */
    public static interface NonIgnorableView {

    }

    /**
     * Used as View to whitelist identification
     */
    public static interface ID {

        /**
         * Used as View to blacklist identification
         */
        public static class Ignore implements State, NonIgnorableView {
        }
    }

    /**
     * Used as View to whitelist state data
     */
    public static interface State {

        /**
         * Used as View to blacklist state data
         */
        public static class Ignore implements ID, NonIgnorableView {
        }
    }
}
