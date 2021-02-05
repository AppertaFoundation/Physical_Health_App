package com.staircase13.apperta.integration.util;

import com.staircase13.apperta.entities.Role;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserTestDto {

    private String username;

    private Role role;

    private String password;

    private String emailAddress;

    private String dateOfBirth;
}
