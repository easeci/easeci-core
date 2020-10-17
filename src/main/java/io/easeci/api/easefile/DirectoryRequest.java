package io.easeci.api.easefile;

import lombok.Data;

@Data
public class DirectoryRequest {
    private String path;
    private boolean force;
}
