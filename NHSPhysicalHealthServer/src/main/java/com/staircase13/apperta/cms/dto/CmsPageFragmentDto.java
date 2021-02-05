package com.staircase13.apperta.cms.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsPageFragmentDto {
    @NotNull
    private String api;

    @NotEmpty
    @Size(max = 255)
    private String primaryEntityName;

    @Size(max = 255)
    private String secondaryEntityName;

    @NotEmpty
    @Size(max = 255)
    private String jsonSectionHeading;

    @NotNull
    private Integer jsonMainEntityPosition;
}
