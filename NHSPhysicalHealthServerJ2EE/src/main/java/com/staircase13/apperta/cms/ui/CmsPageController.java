package com.staircase13.apperta.cms.ui;


import com.staircase13.apperta.cms.CmsService;
import com.staircase13.apperta.cms.LoadLog;
import com.staircase13.apperta.cms.dto.CmsPageDto;
import com.staircase13.apperta.cms.dto.CmsPageFragmentDto;
import com.staircase13.apperta.cms.dto.CmsPageLinkDto;
import com.staircase13.apperta.cms.entities.NhsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.validation.Valid;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

@Controller
public class CmsPageController extends CmsController  {
    private static final String NEW = "new";

    private static final String PAGE_FRAGMENT = "/cms/fragment";
    private static final String PAGE_PAGE = "/cms/page";
    private static final String PAGE_LINK = "/cms/link";
    private static final String PAGE_PREVIEW = "/cms/preview";

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsPageController.class);

    private final CmsUiSession session;

    private final Clock clock;

    private final CmsService cmsService;

    @Autowired
    public CmsPageController(CmsUiSession session, Clock clock, CmsService cmsService) {
        this.session = session;
        this.clock = clock;
        this.cmsService = cmsService;
    }

    @GetMapping("/cms/page/{pageName}")
    public ModelAndView showEditPage(@PathVariable("pageName") String pageName) {
        return new ModelAndView(PAGE_PAGE, "page", getPage(pageName));
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "addConditionsFragment")
    public String addNewConditionsFragment(@PathVariable("pageName") String pageName) {
        return addNewFragment(pageName, NhsApi.CONDITIONS);
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "addLiveWellFragment")
    public String addLiveWellFragment(@PathVariable("pageName") String pageName) {
        return addNewFragment(pageName, NhsApi.LIVE_WELL);
    }

    private String addNewFragment(String pageName, NhsApi api) {
        return redirect(String.format("/cms/page/%s/fragment/%s?forApi=%s", pageName, NEW, api.name()));
    }

    @GetMapping("/cms/page/{pageName}/fragment/{fragmentIndex}")
    public ModelAndView showEditFragment(@PathVariable("pageName") String pageName,
                                        @PathVariable("fragmentIndex") String fragmentIndex,
                                        @RequestParam(value="forApi",required = false) String api) {
        if (isNew(fragmentIndex)) {
           CmsPageFragmentDto fragmentDto = new CmsPageFragmentDto();
           fragmentDto.setApi(api);
           return new ModelAndView(PAGE_FRAGMENT, "fragment", fragmentDto);
        } else {
           return new ModelAndView(PAGE_FRAGMENT, "fragment", getFragment(pageName, fragmentIndex));
        }
    }

    @PostMapping(value = "/cms/page/{pageName}/fragment/{fragmentIndex}", params = "action=cancel")
    public ModelAndView cancelEditFragment(@PathVariable("pageName") String pageName) {
        return new ModelAndView(redirectToEditPage(pageName), "page", getPage(pageName));
    }

    @PostMapping(value = "/cms/page/{pageName}/fragment/{fragmentIndex}", params = "action=submitFragment")
    public ModelAndView submitFragment(@Valid @ModelAttribute("fragment") CmsPageFragmentDto fragment,
                                       BindingResult result,
                                       @PathVariable("pageName") String pageName,
                                       @PathVariable("fragmentIndex") String fragmentIndex) {
        if (result.hasErrors()) {
            LOGGER.debug("Form has errors");
            return new ModelAndView(PAGE_FRAGMENT, "fragment", fragment);
        }

        getPage(pageName).setModified(LocalDateTime.now(clock));
        if(isNew(fragmentIndex)) {
            getPage(pageName).getFragments().add(fragment);
        } else {
            getPage(pageName).getFragments().remove(parseInt(fragmentIndex));
            getPage(pageName).getFragments().add(parseInt(fragmentIndex), fragment);
        }

        session.setModified(true);

        return new ModelAndView(redirectToEditPage(pageName), "page", getPage(pageName));
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "action=editFragment")
    public ModelAndView editFragment(@PathVariable("pageName") String pageName, @RequestParam("index") int fragmentIndex) {
        return new ModelAndView(redirectToEditFragment(pageName, fragmentIndex));
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "action=moveFragmentUp")
    public String moveFragmentUp(@PathVariable("pageName") String pageName, @RequestParam("index") int fragmentIndex) {
        moveUp(getPage(pageName).getFragments(),fragmentIndex);
        return redirectToEditPage(pageName);
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "action=moveFragmentDown")
    public String moveFragmentDown(@PathVariable("pageName") String pageName, @RequestParam("index") int fragmentIndex) {
        moveDown(getPage(pageName).getFragments(),fragmentIndex);
        return redirectToEditPage(pageName);
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "action=deleteFragment")
    public String deleteFragment(@PathVariable("pageName") String pageName, @RequestParam("index") int fragmentIndex) {
        getPage(pageName).getFragments().remove(fragmentIndex);
        session.setModified(true);
        return redirectToEditPage(pageName);
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "addLink")
    public ModelAndView addNewLink(@PathVariable("pageName") String pageName) {
        return new ModelAndView(redirectToEditLink(pageName, NEW));
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "action=editLink")
    public ModelAndView editLink(@PathVariable("pageName") String pageName, @RequestParam("index") String linkIndex) {
        return new ModelAndView(redirectToEditLink(pageName, linkIndex));
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "action=moveLinkUp")
    public String moveLinkUp(@PathVariable("pageName") String pageName, @RequestParam("index") int linkIndex) {
        moveUp(getPage(pageName).getLinks(),linkIndex);
        return redirectToEditPage(pageName);
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "action=moveLinkDown")
    public String moveLinkDown(@PathVariable("pageName") String pageName, @RequestParam("index") int linkIndex) {
        moveDown(getPage(pageName).getLinks(),linkIndex);
        return redirectToEditPage(pageName);
    }

    @PostMapping(value = "/cms/page/{pageName}",params = "action=deleteLink")
    public String deleteLink(@PathVariable("pageName") String pageName, @RequestParam("index") int linkIndex) {
        getPage(pageName).getLinks().remove(linkIndex);
        session.setModified(true);
        return redirectToEditPage(pageName);
    }

    @GetMapping("/cms/page/{pageName}/link/{linkIndex}")
    public ModelAndView showEditLink(@PathVariable("pageName") String pageName,
                                     @PathVariable("linkIndex") String linkIndex) {
        return new ModelAndView(PAGE_LINK, "link", isNew(linkIndex) ? new CmsPageLinkDto() : getLink(pageName,linkIndex));
    }

    @PostMapping(value = "/cms/page/{pageName}/link/{linkIndex}",params = "action=cancel")
    public ModelAndView submitLink(@PathVariable("pageName") String pageName) {
        return new ModelAndView(redirectToEditPage(pageName), "page", getPage(pageName));
    }

    @PostMapping(value = "/cms/page/{pageName}/link/{linkIndex}",params = "action=submitLink")
    public ModelAndView submitLink(@Valid @ModelAttribute("link") CmsPageLinkDto link,
                                          BindingResult result,
                                          @PathVariable("pageName") String pageName,
                                          @PathVariable("linkIndex") String linkIndex) {
        if (result.hasErrors()) {
            LOGGER.debug("Form has errors");
            return new ModelAndView(PAGE_LINK, "link", link);
        }

        getPage(pageName).setModified(LocalDateTime.now(clock));
        if(isNew(linkIndex)) {
            getPage(pageName).getLinks().add(link);
        } else {
            getPage(pageName).getLinks().remove(parseInt(linkIndex));
            getPage(pageName).getLinks().add(parseInt(linkIndex), link);
        }

        session.setModified(true);

        return new ModelAndView(redirectToEditPage(pageName), "page", getPage(pageName));
    }

    @PostMapping(value = "/cms/page/{pageName}", params = "action=editPageInfo")
    public String editPageInfo(@PathVariable("pageName") String pageName) {
        return redirectToEditInfoPage(pageName);
    }

    @PostMapping(value = "/cms/page/{pageName}", params = "action=preview")
    public String preview(@PathVariable("pageName") String pageName) {
        return redirectToPreviewPage(pageName);
    }

    @GetMapping(value = "/cms/page/{pageName}/preview")
    public ModelAndView showPreview(@PathVariable("pageName") String pageName) {
        LoadLog loadLog = new LoadLog();
        CmsPageDto pageDto = getPage(pageName);
        String html = cmsService.previewHtml(pageDto,loadLog);

        Map<String,Object> model = new HashMap<>();
        model.put("html",html);
        model.put("loadLog",loadLog);
        model.put("page",pageDto);
        return new ModelAndView(PAGE_PREVIEW,model);
    }

    public String redirectToEditInfoPage(String pageName) {
        return redirect(String.format("/cms/page/%s/info", pageName));
    }

    private CmsPageFragmentDto getFragment(String pageName, String linkIndex) {
        return session.getPage(pageName).getFragments().get(parseInt(linkIndex));
    }

    private CmsPageLinkDto getLink(String pageName, String linkIndex) {
        return session.getPage(pageName).getLinks().get(parseInt(linkIndex));
    }

    private CmsPageDto getPage(String pageName) {
        return session.getPage(pageName);
    }

    public String redirectToEditPage(String name) {
        return redirect(String.format("/cms/page/%s", name));
    }

    public String redirectToPreviewPage(String name) {
        return redirect(String.format("/cms/page/%s/preview", name));
    }

    private String redirectToEditFragment(String pageName, int fragmentIndex) {
        return redirect(String.format("/cms/page/%s/fragment/%s", pageName, fragmentIndex));
    }

    private String redirectToEditLink(String pageName, String linkIndex) {
        return redirect(String.format("/cms/page/%s/link/%s", pageName, linkIndex));
    }

    private boolean isNew(String string) {
        return string.equalsIgnoreCase(NEW);
    }

    private void moveUp(List<?> list, int index) {
        if(index > 0) {
            Collections.swap(list, index - 1, index);
            session.setModified(true);
        }
    }

    private void moveDown(List<?> list, int index) {
        if(index < list.size() - 1) {
            Collections.swap(list, index, index + 1);
            session.setModified(true);
        }
    }
}
