package com.staircase13.apperta.service.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "CompositionResult", description = "Result from a request for an EHR Composition")
public class CompositionResultDto {

    @ApiModelProperty(notes = "The composition uid requested")
    private String uid;

    @ApiModelProperty(notes = "The resulting ehr composition")
    @JsonRawValue
    private String composition;

    @ApiModelProperty(notes = "If this is the latest version of the composition")
    private boolean isLatest;

    @ApiModelProperty(notes = "Uid of next version if this is not the latest version of the composition")
    private String nextVersion;
}
