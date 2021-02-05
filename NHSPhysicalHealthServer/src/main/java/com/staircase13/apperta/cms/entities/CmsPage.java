package com.staircase13.apperta.cms.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CmsPage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "cmsPage", cascade = CascadeType.ALL)
    @OrderBy("cmsPageOrder")
    private Set<CmsPageFragment> fragments;

    @OneToMany(mappedBy = "cmsPage", cascade = CascadeType.ALL)
    @OrderBy("cmsPageOrder")
    private Set<CmsPageLink> links;

    @NotNull
    @NotEmpty
    @Size(max = 255)
    @Column(unique=true)
    @Pattern(regexp = "[a-zA-Z0-9\\-]+", message = "Page primaryEntityName should be a mix of alpha numeric characters and the '-' symbol")
    private String name;

    @NotNull
    private LocalDateTime modified;

    @NotNull
    @NotEmpty
    @Size(max=255)
    private String linksHeader;

    @PrePersist
    public void prePersist() {
        modified = LocalDateTime.now();
    }
}
