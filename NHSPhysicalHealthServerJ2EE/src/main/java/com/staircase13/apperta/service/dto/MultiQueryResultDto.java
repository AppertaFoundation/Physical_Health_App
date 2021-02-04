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
@ApiModel(value = "MultiQueryResults", description = "Results from an EHR Query")
public class MultiQueryResultDto {

    @ApiModelProperty(notes = "The query results for each query")
    Map<String, EhrQueryResultDto> queryResults;

}
