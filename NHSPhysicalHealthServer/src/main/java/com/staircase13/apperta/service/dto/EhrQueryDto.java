package com.staircase13.apperta.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "Query", description = "Represents the parameters for a query openEhr.")
public class EhrQueryDto {

    @ApiModelProperty(notes = "The columns to select in the query, if no query template is supplied. Comma separators will be inserted between each item.")
    List<String> selectColumns;

    @ApiModelProperty(notes = "The conditional clauses for the query, including AQL contains or where terms, if no parameter map is supplied")
    List<String> whereClauses;

    @ApiModelProperty(notes = "The primaryEntityName of the query template, if one exists, which has previously been registered with the server for the app to request a set of query terms")
    private String queryTemplate;

    @ApiModelProperty(notes = "The user for whom the query should be run, or empty if for the current user")
    private String username;

    @ApiModelProperty(notes = "The primaryEntityName of the parameter map, if one exists, which has previously been registered with the server for the app to map simple query terms to openEhr where clauses")
    private String parameterMap;

    @ApiModelProperty(notes = "The map of parameters to use with the parameter map")
    private Map<String,String> parameters;

    @ApiModelProperty(notes = "The paging start value")
    private int start;

    @ApiModelProperty(notes = "Paging page size")
    private int pagesize;

    @ApiModelProperty(notes = "Start range for time bounded queries")
    private long startDateTime;

    @ApiModelProperty(notes = "End range for time bounded queries")
    private long endDateTime;
}
