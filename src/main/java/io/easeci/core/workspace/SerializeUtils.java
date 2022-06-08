package io.easeci.core.workspace;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;

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

    public static <T> Optional<T> read(byte[] responseAsBytes, Class<T> classType) {
        try {
            return Optional.ofNullable(MAPPER.readValue(responseAsBytes, classType));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}
