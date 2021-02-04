package com.staircase13.apperta.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.staircase13.apperta.entities.DeviceType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "Device", description = "Represents a Device which is used by a User in the api.")
public class DeviceDto {

    @JsonIgnore
    private long id;

    @NotBlank
    @ApiModelProperty(notes = "Unique uuid used to identify the device, device generated", required = true)
    private String uuid;

    // Push registration token
    @ApiModelProperty(notes = "Push token to register for the device", required = true)
    private String token;

    @NotNull
    @ApiModelProperty(notes="The device type: ANDROID/IOS/WEB", required = true)
    private DeviceType type;
}
