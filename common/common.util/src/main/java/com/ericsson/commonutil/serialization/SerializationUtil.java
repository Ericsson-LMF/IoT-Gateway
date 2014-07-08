package com.ericsson.commonutil.serialization;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jdk7.Jdk7Module;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.util.Arrays;
import java.util.regex.Pattern;

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

    public static String execute(Format format, Serializer function) throws SerializationException {
        try {
            return function.apply(get(format));
        } catch (JsonProcessingException ex) {
            throw new SerializationException(ex.getMessage(), ex);
        }
    }

    public static <T> String serializeAccordingPath(Format format, String path, String delimiter, T object) throws SerializationException {
        if (path == null) {
            throw new SerializationException("Path cannot be null");
        }
        JsonNode node = get(format).valueToTree(object);
        String[] split = path.split(Pattern.quote(delimiter));
        for (String pathPiece : split) {
            if (pathPiece.isEmpty()) {
                break;
            }
            node = node.findPath(pathPiece);
            if (node.isMissingNode()) {
                String pathString = Arrays.stream(split).reduce("", (before, after) -> {
                    if (after.equals(pathPiece)) {
                        after = after.toUpperCase();
                    }
                    return before + delimiter + after;
                });
                throw new SerializationException("No such node found (" + pathString + ")");
            }
        }
        return node.toString();
    }
}
