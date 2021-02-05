package com.staircase13.apperta.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
// User is a keyword in Postgres
@Table(name = "AppertaUser")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @NotEmpty
    @NotNull
    @Column(unique=true)
    private String username;

    @Email
    @NotEmpty
    @NotNull
    private String emailAddress;
}
