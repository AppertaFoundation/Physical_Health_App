package com.staircase13.apperta.api;

import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PasswordResetTokenVerifyRequest {
    @NotEmpty
    private String token;
}
