package com.staircase13.apperta.auth.server;

import com.staircase13.apperta.validation.ValidPassword;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class OAuthUser implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @Column(unique=true)
    private String username;

    @ValidPassword
    private String password;

    @Email
    @NotEmpty
    private String emailAddress;
}
