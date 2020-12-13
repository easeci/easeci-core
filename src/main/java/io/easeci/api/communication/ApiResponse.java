package io.easeci.api.communication;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.checkerframework.checker.units.qual.A;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private ApiStatus status;
    private String domainStatus;
    private String message;
    private T payload;

    private ApiResponse(ApiStatus apiStatus, T payload) {
        this.status = apiStatus;
        this.payload = payload;
    }

    private ApiResponse(ApiStatus apiStatus, String domainStatus, String message) {
        this.status = apiStatus;
        this.domainStatus = domainStatus;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(T payload) {
        return new ApiResponse<>(ApiStatus.SUCCESS, payload);
    }

    public static <T> ApiResponse<T> failure(String domainStatus, String message) {
        return new ApiResponse<>(ApiStatus.FAILURE, domainStatus, message);
    }

    public static <T> ApiResponse<T> unknownFailure() {
        final GlobalDomainError domainStatus = GlobalDomainError.UNEXPECTED_SERVER_ERROR;
        final String message = domainStatus.message();
        return new ApiResponse<>(ApiStatus.FAILURE, domainStatus.name(), message);
    }

    public static <T> ApiResponse<T> unknownSuccess() {
        final GlobalDomainError domainStatus = GlobalDomainError.UNEXPECTED_SUCCESS;
        final String message = domainStatus.message();
        return new ApiResponse<>(ApiStatus.SUCCESS, domainStatus.name(), message);
    }
}
