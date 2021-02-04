package com.staircase13.apperta.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.staircase13.apperta.ehrconnector.HcpDemographicsDto;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "Profile", description = "Represents a User Profile of the API")
public class ProfileDto {

    @ApiModelProperty(notes = "Username")
    private String username;

    @NotEmpty
    @ApiModelProperty(notes = "Title")
    private String title;

    @NotEmpty
    @ApiModelProperty(notes = "First primaryEntityName(s)")
    private String firstNames;

    @NotEmpty
    @ApiModelProperty(notes = "Last primaryEntityName")
    private String lastName;

    @NotEmpty
    @ApiModelProperty(notes = "Users date of birth")
    private String dateOfBirth;

    @ApiModelProperty(notes = "Users address")
    private String address;

    @ApiModelProperty(notes = "Mobile number")
    private String mobileNumber;

    @ApiModelProperty(notes = "Second contact number")
    private String telNumber;

    @NotEmpty
    @ApiModelProperty(notes = "Gender, valid responses MALE / FEMALE / OTHER")
    private String gender;

    @ApiModelProperty(notes = "Gender code, NHS AdministrativeGender coding")
    private int genderCode;

    @ApiModelProperty(notes = "NHS Number")
    private String nhsNumber;

    @JsonIgnore
    private HcpDemographicsDto hcps;
}
