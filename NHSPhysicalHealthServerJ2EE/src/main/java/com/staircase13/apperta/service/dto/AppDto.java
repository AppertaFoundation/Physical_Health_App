package com.staircase13.apperta.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "Application", description = "Represents an application registered with the service. Using this app reference, the application can register the EHR templates that are required for it to operate and store and pre-prepared query templates and composition element names to make future calls easier to make.")
public class AppDto {

    @ApiModelProperty(notes = "The application administrative primaryEntityName", required = true)
    @NotEmpty
    private String appName;

    @ApiModelProperty(notes = "The list of ehr templates which are required for the application to be able to run")
    private List<String> requiredEhrTemplates;

    @ApiModelProperty(notes = "A list of named queries")
    private List<QueryTemplateDto> queryTemplates;

    @ApiModelProperty(notes = "A list of parameter replacements that can be used for ehr operations to simplify the requests sent by the app")
    private List<CompositionParametersDto> compositionParameters;
}
