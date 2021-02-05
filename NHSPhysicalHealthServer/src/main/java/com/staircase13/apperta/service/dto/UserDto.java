package com.staircase13.apperta.service.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.staircase13.apperta.entities.Role;
import com.staircase13.apperta.validation.ValidPassword;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "User", description = "Represents a User of the API")
public class UserDto {

    @NotEmpty
    @ApiModelProperty(notes = "Unique username use to identify the user", required = true)
    private String username;

    @NotNull
    @ApiModelProperty(notes = "The user's role", required = true)
    private Role role;

    @ValidPassword
    @ApiModelProperty(notes = "Password that has a minimum length of 255 characters and a mix of upper and lower case letters", required = true)
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private String password;

    @Email
    @NotEmpty
    @ApiModelProperty(notes = "Users email address")
    private String emailAddress;

    @ApiModelProperty(notes = "Password that has no length/complexity restrictions", required = true)
    @JsonProperty("password")
    public void setPassword(String pw) {
        this.password = pw;
    }

    @JsonIgnore
    public String getPassword() {
        return this.password;
    }
}
