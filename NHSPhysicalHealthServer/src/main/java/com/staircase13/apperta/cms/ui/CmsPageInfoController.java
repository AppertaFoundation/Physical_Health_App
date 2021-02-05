package com.staircase13.apperta.cms.ui;


import com.staircase13.apperta.cms.dto.CmsPageDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.time.Clock;
import java.time.LocalDateTime;

@Controller
public class CmsPageInfoController extends CmsController {

    private static final String MODEL_NAME = "pageInfo";
    private static final String PAGE_INFO_PAGE = "/cms/pageInfo";

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsPageInfoController.class);

    private final CmsUiSession session;

    private final CmsPageController cmsPageController;

    private final CmsPagesController cmsPagesController;

    private final Clock clock;


    @Autowired
    public CmsPageInfoController(CmsUiSession session, CmsPageController cmsPageController, CmsPagesController cmsPagesController, Clock clock) {
        this.session = session;
        this.cmsPageController = cmsPageController;
        this.cmsPagesController = cmsPagesController;
        this.clock = clock;
    }

    @GetMapping("/cms/addPage")
    public ModelAndView addPage() {
        CmsPageInfoDto dto = new CmsPageInfoDto();
        dto.setExisting(false);
        return new ModelAndView(PAGE_INFO_PAGE,MODEL_NAME,dto);
    }

    @PostMapping(value="/cms/addPage",params = "action=cancel")
    public String cancel() {
        return cmsPagesController.redirectToPages();
    }

    @PostMapping(value="/cms/addPage",params = "action=submit")
    public ModelAndView addPage(@Valid @ModelAttribute(MODEL_NAME) CmsPageInfoDto cmsPageInfoDto,
                                BindingResult result) {
        if (result.hasErrors()) {
            LOGGER.debug("Form has errors");
            return new ModelAndView(PAGE_INFO_PAGE, MODEL_NAME, cmsPageInfoDto);
        }

        if(session.getPages().stream().anyMatch(p -> p.getName().equalsIgnoreCase(cmsPageInfoDto.getName()))) {
            result.rejectValue("name",null,"A page with the given name already exists");
            return new ModelAndView(PAGE_INFO_PAGE, MODEL_NAME, cmsPageInfoDto);
        }

        int index = session.getPages().size();
        CmsPageDto page = new CmsPageDto();
        page.setName(cmsPageInfoDto.getName());
        page.setLinksHeader(cmsPageInfoDto.getLinksHeader());
        page.setModified(LocalDateTime.now(clock));
        session.getPages().add(page);
        session.sortPages();

        session.setModified(true);

        return new ModelAndView(cmsPageController.redirectToEditPage(page.getName()));
    }

    @GetMapping(value = "/cms/page/{pageName}/info")
    public ModelAndView showEditPageInfo(@PathVariable("pageName") String pageName) {

        CmsPageDto page = session.getPage(pageName);

        CmsPageInfoDto dto = new CmsPageInfoDto();
        dto.setExisting(true);
        dto.setName(page.getName());
        dto.setLinksHeader(page.getLinksHeader());

        return new ModelAndView(PAGE_INFO_PAGE, MODEL_NAME, dto);
    }

    @PostMapping(value="/cms/page/{pageName}/info",params = "action=submit")
    public ModelAndView saveChanges(@PathVariable("pageName") String pageName,
                                    @Valid @ModelAttribute(MODEL_NAME) CmsPageInfoDto cmsPageInfoDto,
                                    BindingResult result) {

        if (result.hasFieldErrors("linksHeader")) {
            LOGGER.debug("Form has errors");
            return new ModelAndView(PAGE_INFO_PAGE, MODEL_NAME, cmsPageInfoDto);
        }

        session.getPage(pageName).setLinksHeader(cmsPageInfoDto.getLinksHeader());

        session.setModified(true);

        return new ModelAndView(cmsPageController.redirectToEditPage(pageName));
    }
}
