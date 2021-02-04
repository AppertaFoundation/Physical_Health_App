package com.staircase13.apperta.cms.ui;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CmsPageInfoDto {
    private boolean existing;

    @NotEmpty
    @Pattern(regexp = "[a-zA-Z0-9\\-]+", message = "Page primaryEntityName should be a mix of alpha numeric characters and the '-' symbol")
    private String name;

    @NotEmpty
    @Size(max=255)
    private String linksHeader;
}
