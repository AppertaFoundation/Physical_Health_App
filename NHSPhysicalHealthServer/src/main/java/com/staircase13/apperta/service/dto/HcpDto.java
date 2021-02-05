package com.staircase13.apperta.service.dto;

import lombok.*;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HcpDto {
    @NotEmpty
    private String username;

    @Email
    @NotEmpty
    private String email;

    @NotEmpty
    private String nhsId;

    @NotEmpty
    private String title;

    @NotEmpty
    private String firstNames;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String jobTitle;

    @NotEmpty
    private String location;
}
