package com.staircase13.apperta.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Hcp {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotEmpty
    @NotNull
    @Column(unique=true)
    private String nhsId;

    @OneToOne
    private User user;

    @NotEmpty
    @NotNull
    private String title;

    @NotEmpty
    @NotNull
    private String firstNames;

    @NotEmpty
    @NotNull
    private String lastName;

    @NotEmpty
    @NotNull
    private String jobTitle;

    @NotEmpty
    @NotNull
    private String location;
}
