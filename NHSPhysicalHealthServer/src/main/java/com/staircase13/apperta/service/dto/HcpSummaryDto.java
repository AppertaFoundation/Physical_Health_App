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
@ApiModel(value = "Healthcare Professional Summary", description = "Represents a healthcare professional who is associated with a User in the api.")
public class HcpSummaryDto {
    @NotEmpty
    @ApiModelProperty(notes = "NHS id", required = true)
    private String nhsId;

    @NotEmpty
    @ApiModelProperty(notes = "HCP title", required = true)
    private String title;

    @NotEmpty
    @ApiModelProperty(notes = "HCP first names", required = true)
    private String firstNames;

    @NotEmpty
    @ApiModelProperty(notes = "HCP last primaryEntityName", required = true)
    private String lastName;

    @NotEmpty
    @ApiModelProperty(notes = "HCP Job title", required = true)
    private String jobTitle;

    @NotEmpty
    @ApiModelProperty(notes = "HCP department, trust, or location", required = true)
    private String location;
}
