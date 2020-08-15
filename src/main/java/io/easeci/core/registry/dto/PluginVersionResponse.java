package io.easeci.core.registry.dto;

import lombok.Data;

@Data
public class PluginVersionResponse {
    private Long versionId;
    private String performerVersion;
    private long performerScriptBytes;
    private Boolean validated;
    private String releaseDate;
    private String documentationUrl;
}
