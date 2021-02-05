package com.staircase13.apperta.cms.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CmsCache {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NhsApi api;

    @NotNull
    @Size(max=255)
    private String primaryEntityName;

    @Size(max = 255)
    private String secondaryEntityName;

    @NotEmpty
    @Lob
    private String content;

    @NotNull
    private LocalDateTime loaded;
}
