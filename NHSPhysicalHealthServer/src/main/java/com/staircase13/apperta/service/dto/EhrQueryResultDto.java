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
@ApiModel(value = "QueryResults", description = "Results from an EHR Query")
public class EhrQueryResultDto {

    @ApiModelProperty(notes = "The EHR query result set, which will consist of a JSON Array of JSON objects containing each result point.")
    @JsonRawValue
    private String resultSet;

    @ApiModelProperty(notes = "The paging start value")
    private int start;

    @ApiModelProperty(notes = "Paging page size")
    private int pagesize;
}
