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
@ApiModel(value = "QueryTemplate", description = "Represents a query template which can be used to store a preformatted ehr query containing parameters.")
public class QueryTemplateDto {

    @ApiModelProperty(notes = "The primaryEntityName associated with the template, must be unique throughout the system", required = true)
    @NotEmpty
    private String name;

    @ApiModelProperty(notes = "The query string, including any templated parameters in moustache format", required = true)
    @NotEmpty
    private String template;
}
