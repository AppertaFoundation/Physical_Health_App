package com.staircase13.apperta.physicalhealth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.staircase13.apperta.ehrconnector.interfaces.IAppRegistration;
import com.staircase13.apperta.service.AppService;
import com.staircase13.apperta.service.dto.AppDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * This application is currently written as a monolithic server, but is designed with the intention that it may be split
 * into a microservices architecture in the future. At that time, this class would be within a separate service application
 * and would use the external app interface to register itself with the core services.
 */
@Component
public class PhysicalHealthAppConfig implements IAppRegistration {

    private static final Logger LOGGER = LoggerFactory.getLogger(PhysicalHealthAppConfig.class);

    private final static String PHYSICAL_HEALTH_APP_NAME = "AppertaPhsHealth";

    private final AppService appService;

    private final ResourceLoader resourceLoader;

    @Autowired
    public PhysicalHealthAppConfig(AppService appService, ResourceLoader resourceLoader) {
        this.appService = appService;
        this.resourceLoader = resourceLoader;
    }


    @Override
    public void runAppRegistration() {
// TODO: handle not valid exceptions?
        if (!appService.checkAppExists(PHYSICAL_HEALTH_APP_NAME)) {

            AppDetailsResource appDetailsResource = loadAppDetailsJson();

            AppDto appDto = AppDto.builder().appName(PHYSICAL_HEALTH_APP_NAME)
                    .requiredEhrTemplates(appDetailsResource.getTemplates())
                    .queryTemplates(appDetailsResource.getQueries())
                    .compositionParameters(appDetailsResource.getParams())
                    .build();
            appService.registerApp(appDto);
        }
    }

    private AppDetailsResource loadAppDetailsJson() {

        try {
            Resource appDetailsResource = resourceLoader.getResource("classpath:/physical.health/app.json");
            File appDetailsFile = appDetailsResource.getFile();

            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(appDetailsFile, AppDetailsResource.class);

        } catch (IOException ioe) {
            LOGGER.error("Could not load app resource details!");
        }
        return null;
    }
}
