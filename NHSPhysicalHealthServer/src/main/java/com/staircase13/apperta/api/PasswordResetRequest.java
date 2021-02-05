package com.staircase13.apperta.api;

import com.staircase13.apperta.validation.ValidPassword;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PasswordResetRequest {
    @ValidPassword
    private String password;
}
