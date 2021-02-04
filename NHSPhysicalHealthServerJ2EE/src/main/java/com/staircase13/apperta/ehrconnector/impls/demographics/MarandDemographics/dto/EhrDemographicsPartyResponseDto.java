package com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.staircase13.apperta.ehrconnector.impls.MarandBase.EhrBaseResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrDemographicsPartyResponseDto extends EhrBaseResponse {

    private EhrDemographicsDto party;

}
