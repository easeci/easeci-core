package io.easeci.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelName.TECHNICAL_EVENT;
import static io.easeci.core.log.ApplicationLevelLogFacade.LogLevelPrefix.FIVE;
import static io.easeci.core.log.ApplicationLevelLogFacade.logit;

public class ApiUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static byte[] write(Object o) {
        try {
            return objectMapper.writeValueAsBytes(o);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logit(TECHNICAL_EVENT, "Exception occurred while trying to serialize object to byte array, exception: " + e.getMessage(), FIVE);
            return new byte[] {};
        }
    }
}
