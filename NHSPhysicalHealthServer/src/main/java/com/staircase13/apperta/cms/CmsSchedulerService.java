package com.staircase13.apperta.cms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class CmsSchedulerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CmsSchedulerService.class);

    private final CmsService cmsService;

    @Autowired
    public CmsSchedulerService(CmsService cmsService) {
        this.cmsService = cmsService;
    }

    @Scheduled(cron = "${apperta.cms.tar.rebuild.schedule.cron}")
    public void updateTar() {
        LOGGER.info("Starting scheduled job to rebuild CMS tar");
        cmsService.rebuildTar();
    }
}
