package com.staircase13.apperta.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CompositionParameters implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    private DeviceApp app;

    @NotEmpty
    @NotNull
    private String setName;

    @NotEmpty
    @NotNull
    @Column(length = 500)
    private String parameterName;

    @NotEmpty
    @NotNull
    @Column(length = 2000)
    private String ehrName;

}
