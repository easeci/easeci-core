package io.easeci.core.engine.runtime;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@Data
@ToString
@EqualsAndHashCode(callSuper = true)
public class PipelineContextInfo extends ContextInfo {
    private Date creationDate;
    private Date finishDate;
}
