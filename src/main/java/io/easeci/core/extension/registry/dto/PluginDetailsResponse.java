package io.easeci.core.extension.registry.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PluginDetailsResponse extends BaseRegistryResponse {
    private Long performerId;
    private String authorFullname;
    private String authorEmail;
    private String company;
    private String creationDate;
    private String performerName;
    private String description;
    private boolean isNewerVersionAvailable;
    private byte[] documentationText;
    private List<PluginVersionBasic> newerPerformerVersions;
    private Set<PluginVersionResponse> performerVersions;
}
