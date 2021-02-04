package com.staircase13.apperta.cms.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CmsPageLink {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CmsPage cmsPage;

    @NotNull
    private int cmsPageOrder;

    @Pattern(regexp = "https?:\\/\\/.*", message = "URL should start with http:// or https://")
    @Size(max = 255)
    private String url;

    @NotNull
    @NotEmpty
    @Size(max = 255)
    private String label;

    @NotNull
    @NotEmpty
    @Size(max = 255)
    private String description;
}
