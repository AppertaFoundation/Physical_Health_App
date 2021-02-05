package com.staircase13.apperta.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "Composition Parameters", description = "Represents the a query template which can be used to store a preformatted ehr query.")
public class CompositionParametersDto {

    @ApiModelProperty(notes = "A primaryEntityName for grouping a set of parameters which will be used together in a composition", required = true)
    @NotEmpty
    private String setName;

    @ApiModelProperty(notes = "The friendly primaryEntityName of the parameter to be sent by the client app", required = true)
    @NotEmpty
    private String parameterName;

    @ApiModelProperty(notes = "The corresponding EHR string to be used in place of the friendly primaryEntityName in composition creation", required = true)
    @NotEmpty
    private String ehrName;
}
