package com.staircase13.apperta.ui;

import com.staircase13.apperta.validation.ValidPassword;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@MatchingPasswords
public class PasswordResetRequest {
    @NotEmpty
    private String resetToken;
    @ValidPassword
    private String password;
    private String passwordRepeated;
}
