package io.easeci.core.workspace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SerializeUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static byte[] write(Object object) {
        try {
            return MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return new byte[] {};
        }
    }
}
