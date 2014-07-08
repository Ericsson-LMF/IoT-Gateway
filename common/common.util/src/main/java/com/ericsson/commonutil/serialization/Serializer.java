package com.ericsson.commonutil.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author aopkarja
 */
@FunctionalInterface
public interface Serializer {

    String apply(ObjectMapper mapper) throws JsonProcessingException;

}
