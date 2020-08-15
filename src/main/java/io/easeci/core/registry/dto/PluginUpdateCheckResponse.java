package io.easeci.core.registry.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class PluginUpdateCheckResponse extends BaseRegistryResponse {
    private boolean isNewerVersionAvailable;
    private List<PluginVersionBasic> newerPerformerVersions;
}
