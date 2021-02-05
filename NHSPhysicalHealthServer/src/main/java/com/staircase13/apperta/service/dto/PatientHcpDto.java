package com.staircase13.apperta.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "Patient Healthcare Professionals", description = "Represents the relationships between a patient and their related healthcare professionals.")
public class PatientHcpDto {

    @ApiModelProperty(notes = "The primary care professional associated with the patient and application", required = true)
    HcpSummaryDto primaryCareProfessional;

    @ApiModelProperty(notes = "A list of all other care professionals who the patient has associated with his care record", required = true)
    List<HcpSummaryDto> careProfessionals;

}
