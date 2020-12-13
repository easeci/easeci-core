package io.easeci.api.projects;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SuccessResponse {
    private Long id;
    private ProjectDomainStatus status;
    private String message;

    private SuccessResponse(Long id, ProjectDomainStatus status, String message) {
        this.id = id;
        this.status = status;
        this.message = message;
    }

    public static SuccessResponse of(Long id, ProjectDomainStatus status) {
        return new SuccessResponse(id, status, status.message());
    }

    public static SuccessResponse of(ProjectDomainStatus status) {
        return new SuccessResponse(null, status, status.message());
    }
}
