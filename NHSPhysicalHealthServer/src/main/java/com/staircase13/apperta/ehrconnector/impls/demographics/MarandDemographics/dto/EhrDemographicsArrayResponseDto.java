package com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.staircase13.apperta.ehrconnector.impls.MarandBase.EhrBaseResponse;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrDemographicsArrayResponseDto extends EhrBaseResponse {

    private List<EhrDemographicsDto> parties;

}
