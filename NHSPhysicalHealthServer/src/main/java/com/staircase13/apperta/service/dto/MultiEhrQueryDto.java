package com.staircase13.apperta.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "MultiQuery", description = "A wrapper which allows you to sent multiple queries to the server to be processed together, each associated with a name")
public class MultiEhrQueryDto {

    @ApiModelProperty(notes = "The queries to run")
    Map<String, EhrQueryDto> queries;

    @ApiModelProperty(notes = "The user for whom the queries should be run, or empty if for the current user")
    private String username;
}
