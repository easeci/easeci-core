package io.easeci.commons;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
public class SerializeUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static byte[] write(Object object) {
        try {
            return MAPPER.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while writing object to json", e);
            return new byte[] {};
        }
    }

    public static String prettyWrite(Object object) {
        try {
            return MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error occurred while writing object to json", e);
            throw new RuntimeException("Serialization failed");
        }
    }

    public static <T> Optional<T> read(byte[] responseAsBytes, Class<T> classType) {
        try {
            return Optional.ofNullable(MAPPER.readValue(responseAsBytes, classType));
        } catch (IOException e) {
            log.error("Error occurred while reading json as object", e);
            return Optional.empty();
        }
    }

    public static <T> Optional<T> read(byte[] responseAsBytes, TypeReference<T> typeReference) {
        try {
            return Optional.ofNullable(MAPPER.readValue(responseAsBytes, typeReference));
        } catch (IOException e) {
            log.error("Error occurred while reading json as object", e);
            return Optional.empty();
        }
    }

    public static <T> Optional<T> read(File file, Class<T> classType) {
        try {
            return Optional.ofNullable(MAPPER.readValue(file, classType));
        } catch (IOException e) {
            log.error("Error occurred while reading json as object", e);
            return Optional.empty();
        }
    }
}
