package com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.staircase13.apperta.ehrconnector.interfaces.IDemographicsDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrDemographicsDto implements IDemographicsDto {

    private String firstNames;

    private String lastNames;

    private String gender;

    private String dateOfBirth;

    private EhrAddress address;

    private List<AdditionalInfo> partyAdditionalInfo;

    @JsonIgnore
    private long partyId;

    @JsonIgnore
    public long getPartyId() {
        return partyId;
    }

    @JsonProperty("id")
    public void setPartyId(long partyId){
        this.partyId = partyId;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EhrAddress {

        String address;

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AdditionalInfo {

        String key;

        String value;
    }

}
