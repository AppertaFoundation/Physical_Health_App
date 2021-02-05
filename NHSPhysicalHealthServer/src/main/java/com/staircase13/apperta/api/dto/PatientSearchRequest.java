package com.staircase13.apperta.api.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "PatientSearchResponse", description = "Returns a patient search from an HCP patient search")
public class PatientSearchRequest {

    @ApiModelProperty(notes = "Search terms")
    private Map<String, String> searchTerms;

    @ApiModelProperty(notes = "Paging page size")
    private int pageSize;

    @ApiModelProperty(notes = "Paging start record")
    private int start;
}
