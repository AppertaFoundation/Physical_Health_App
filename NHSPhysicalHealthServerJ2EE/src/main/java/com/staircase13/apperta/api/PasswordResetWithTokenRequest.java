package com.staircase13.apperta.api;

import com.staircase13.apperta.validation.ValidPassword;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetWithTokenRequest {
    @NotEmpty
    private String token;
    @ValidPassword
    private String password;
}
