package io.easeci.api.easefile;

import lombok.Data;

@Data
public class AddEasefileRequest {
    private String path;
    private String encodedEasefileContent;
}
