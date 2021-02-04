package com.staircase13.apperta.cms.ui;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice(basePackages = "com.staircase13.apperta.cms.ui")
public class CmsControllerAdvice {

    private final CmsUiSession cmsUiSession;

    public CmsControllerAdvice(CmsUiSession cmsUiSession) {
        this.cmsUiSession = cmsUiSession;
    }

    @ModelAttribute(value = "cmsUiSession")
    public CmsUiSession getCmsUiSession() {
        return cmsUiSession;
    }

}
