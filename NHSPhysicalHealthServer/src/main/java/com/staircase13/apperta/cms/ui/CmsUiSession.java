package com.staircase13.apperta.cms.ui;

import com.staircase13.apperta.cms.dto.CmsPageDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Getter
@Setter
public class CmsUiSession {
    private boolean initialised = false;
    private boolean modified = false;

    private List<CmsPageDto> pages;

    public CmsUiSession() {
        pages = new ArrayList<>();
    }

    public void sortPages() {
        pages = pages.stream().sorted((a,b) -> a.getName().compareToIgnoreCase(b.getName())).collect(toList());
    }

    public CmsPageDto getPage(String name) {
        return pages.stream().filter(page -> page.getName().equalsIgnoreCase(name)).findFirst().get();
    }

    public boolean isInitialised() {
        return initialised;
    }

    public void setInitialised(boolean initialised) {
        this.initialised = initialised;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }
}
