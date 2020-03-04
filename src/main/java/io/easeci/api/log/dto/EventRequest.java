package io.easeci.api.log.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventRequest {

    @NotNull
    @NotBlank
    @Length(max = 2000)
    private String title;

    @NotNull
    @NotBlank
    @Length(max = 10000)
    private String content;
}
