package com.ericsson.common.util.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 *
 * @author delma
 * @param <T>
 */
@FunctionalInterface
public interface Serializer<T> {

    T apply(ObjectMapper mapper) throws IOException;

}
