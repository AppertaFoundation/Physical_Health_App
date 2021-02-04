package com.staircase13.apperta.cms.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@ToString
public class CmsPageFragment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private CmsPage cmsPage;

    @NotNull
    private int cmsPageOrder;

    @NotNull
    @Enumerated(EnumType.STRING)
    private NhsApi api;

    /**
     * For Conditions API, this is the condition
     * For Live Well API, this is the Topic
     */
    @NotNull
    @NotEmpty
    @Size(max = 255)
    private String primaryEntityName;

    /**
     * An optional Topic Page
     */
    @Size(max = 255)
    private String secondaryEntityName;

    @NotNull
    @NotEmpty
    @Size(max = 255)
    private String jsonSectionHeading;

    @NotNull
    private int jsonMainEntityPosition;
}
