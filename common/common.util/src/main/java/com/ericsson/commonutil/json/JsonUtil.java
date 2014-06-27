package com.ericsson.commonutil.json;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;

/**
 *
 * @author aopkarja
 */
public enum JsonUtil {

    /**
     * Singleton.
     */
    INSTANCE;

    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        MAPPER.registerModule(new Jdk7Module());
        MAPPER.registerModule(new ParameterNamesModule());
        MAPPER.registerModule(new MrBeanModule());

        MAPPER.configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, false);
        MAPPER.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        MAPPER.setSerializationInclusion(Include.NON_EMPTY);
//        MAPPER.configure(MapperFeature.DEFAULT_VIEW_INCLUSION, false);
    }

    public static ObjectMapper get() {
        return MAPPER;
    }

    public static <T> T execute(Mapper<T> executable) throws IOException {
        return executable.apply(MAPPER);
    }

    public interface Mapper<T> {

        T apply(ObjectMapper mapper) throws JsonProcessingException;
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
