package com.staircase13.apperta.api;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetTokenRequest {
    @NotEmpty
    private String username;
}
