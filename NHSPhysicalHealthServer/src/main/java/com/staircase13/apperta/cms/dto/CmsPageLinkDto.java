package com.staircase13.apperta.cms.dto;

import lombok.*;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmsPageLinkDto {
    private Integer cmsPageOrder;

    @Pattern(regexp = "https?:\\/\\/.*", message = "URL should start with http:// or https://")
    @Size(max = 255)
    private String url;

    @NotEmpty
    @Size(max = 255)
    private String label;

    @NotEmpty
    @Size(max = 255)
    private String description;
}
