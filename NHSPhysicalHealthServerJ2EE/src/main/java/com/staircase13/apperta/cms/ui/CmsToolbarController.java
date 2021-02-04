package com.staircase13.apperta.cms.ui;


import com.staircase13.apperta.cms.CmsService;
import com.staircase13.apperta.cms.LoadLog;
import com.staircase13.apperta.cms.api.CmsApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.file.Path;

@Controller
public class CmsToolbarController {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmsToolbarController.class);

    private static final String PAGE_LOAD_LOG = "/cms/loadLog";

    private final CmsService cmsService;

    private final CmsUiSession session;

    private final CmsPagesController cmsPagesController;

    @Autowired
    public CmsToolbarController(CmsService cmsService, CmsUiSession session, CmsPagesController cmsPagesController) {
        this.cmsService = cmsService;
        this.session = session;
        this.cmsPagesController = cmsPagesController;
    }

    @PostMapping(value = "/cms/actions",params = "action=commitChanges")
    public ModelAndView commitChanges(RedirectAttributes redirectAttrs) {
        LoadLog loadLog = cmsService.saveChanges(session.getPages());
        cmsPagesController.rebuildSession();
        return showLoadLog(loadLog,"Changes committed. Please check load log below for issues (highlighted in red).");
    }

    @PostMapping(value = "/cms/actions",params = "action=downloadTar")
    public ModelAndView downloadTar(HttpServletResponse httpServletResponse) throws IOException {
        CmsService.TemporaryTarResponse response = cmsService.getTemporaryTar(session.getPages());
        if(response.getTar().isPresent()) {
            Path tar = response.getTar().get();

            CmsApi.downloadTar(tar,httpServletResponse);
            return null;
        } else {
            return showLoadLog(response.getLoadLog(),"There was an issue generating the TAR. Please review the load log below.");
        }
    }

    @PostMapping(value = "/cms/actions",params = "action=viewLoadLog")
    public ModelAndView viewLoadLog() throws IOException {
        LoadLog loadLog = cmsService.getLoadLog(session.getPages());
        return showLoadLog(loadLog,"The Load Log shown below includes any uncommitted changes");
    }

    @PostMapping(value = "/cms/actions",params = "action=flushCache")
    public ModelAndView flushCache() {
        LoadLog loadLog = cmsService.flushCache();
        cmsPagesController.rebuildSession();
        return showLoadLog(loadLog,"Cache flushed. Please check load log below for issues (highlighted in red).");
    }

    private ModelAndView showLoadLog(LoadLog loadLog, String message) {
        ModelAndView modelAndView = new ModelAndView(PAGE_LOAD_LOG);
        modelAndView.addObject("loadLog",loadLog);
        modelAndView.addObject("message", message);
        return modelAndView;
    }
}
