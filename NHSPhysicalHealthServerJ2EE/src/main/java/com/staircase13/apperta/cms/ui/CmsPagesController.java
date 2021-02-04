package com.staircase13.apperta.cms.ui;


import com.staircase13.apperta.cms.CmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@Controller
public class CmsPagesController {
    private static final String PAGE_PAGES = "/cms/pages";

    private final CmsService cmsService;

    private final CmsUiSession session;

    private final CmsPageController cmsPageController;

    @Autowired
    public CmsPagesController(CmsService cmsService, CmsUiSession session, CmsPageController cmsPageController) {
        this.cmsService = cmsService;
        this.session = session;
        this.cmsPageController = cmsPageController;
    }

    @GetMapping("/cms/pages")
    public ModelAndView getPages() {
        if(!session.isInitialised()) {
            rebuildSession();
        }

        Map<String,Object> model = new HashMap<>();
        model.put("pages", session.getPages());
        return new ModelAndView(PAGE_PAGES,model);
    }

    public void rebuildSession() {
        session.setModified(false);
        session.setPages(cmsService.getPages());
        session.sortPages();
        session.setInitialised(true);
    }

    @PostMapping(value="/cms/pages",params = "action=deletePage")
    public String deletePage(@RequestParam("name") String name) {
        session.getPages().remove(session.getPage(name));
        return redirectToPages();
    }

    @PostMapping(value="/cms/pages",params = "action=editPage")
    public String editPage(@RequestParam("name") String name) {
        return cmsPageController.redirectToEditPage(name);
    }

    @GetMapping("/cms")
    public String redirectToPages() {
        return redirect(PAGE_PAGES);
    }

    private String redirect(String target) {
        return "redirect:" + target;
    }

}
