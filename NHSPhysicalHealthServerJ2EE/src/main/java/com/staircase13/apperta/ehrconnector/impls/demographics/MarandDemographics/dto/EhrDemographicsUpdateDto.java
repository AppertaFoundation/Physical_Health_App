package com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto;

import com.fasterxml.jackson.annotation.JsonProperty;


public class EhrDemographicsUpdateDto extends EhrDemographicsDto {

    @JsonProperty("id")
    private long partyId;

    public EhrDemographicsUpdateDto(EhrDemographicsDto dto) {
        super(dto.getFirstNames(), dto.getLastNames(), dto.getGender(), dto.getDateOfBirth(), dto.getAddress(), dto.getPartyAdditionalInfo(), dto.getPartyId());
        partyId = dto.getPartyId();
    }
}
