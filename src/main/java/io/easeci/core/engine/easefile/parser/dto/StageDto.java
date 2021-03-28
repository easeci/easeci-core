package io.easeci.core.engine.easefile.parser.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class StageDto {

    @JsonProperty("stage_name")
    private String stageName;
    private List<String> steps;
    @JsonProperty("stage_variables")
    private Map<String, Object> variables;
}
