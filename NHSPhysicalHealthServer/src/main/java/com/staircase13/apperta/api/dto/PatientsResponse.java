package com.staircase13.apperta.api.dto;

import com.staircase13.apperta.service.dto.ProfileDto;
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
@ApiModel(value = "PatientSearchResponse", description = "Returns a patient search from an HCP patient search")
public class PatientsResponse {

    @ApiModelProperty(notes = "Search terms which produced results")
    private Map<String,String> searchTerms;

    @ApiModelProperty(notes = "Paging page size")
    private int pageSize;

    @ApiModelProperty(notes = "Paging start record")
    private int start;

    @ApiModelProperty(notes = "List of patient profiles")
    private List<ProfileDto> profiles;
}
