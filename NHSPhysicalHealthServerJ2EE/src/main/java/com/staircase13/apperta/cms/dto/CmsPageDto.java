package com.staircase13.apperta.cms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CmsPageDto {
    private List<CmsPageFragmentDto> fragments;

    private List<CmsPageLinkDto> links;

    public CmsPageDto() {
        fragments = new ArrayList<>();
        links = new ArrayList<>();
    }

    @NotEmpty
    @Size(max = 255)
    @Pattern(regexp = "[a-zA-Z0-9\\-]+", message = "Page primaryEntityName should be a mix of alpha numeric characters and the '-' symbol (no spaces allowed)")
    private String name;

    private LocalDateTime modified;

    @NotEmpty
    @Size(max=255)
    private String linksHeader;
}
