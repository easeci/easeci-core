package io.easeci.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
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

    public static <T> Optional<T> read(byte[] responseAsBytes, TypeReference<T> typeReference) {
        try {
            return Optional.ofNullable(MAPPER.readValue(responseAsBytes, typeReference));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static <T> Optional<T> read(File file, Class<T> classType) {
        try {
            return Optional.ofNullable(MAPPER.readValue(file, classType));
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }
}