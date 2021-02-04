package com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics;

import com.staircase13.apperta.ehrconnector.ConfigConstants;
import com.staircase13.apperta.ehrconnector.impls.MarandBase.BaseConnector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static com.staircase13.apperta.ehrconnector.ConfigConstants.EHR_DEMOGRAPHICS_BASE_URL;

@Component
public class DemographicsConnector extends BaseConnector {
    @Autowired
    public DemographicsConnector(Environment environment, RestTemplate restTemplate) {
        super(environment, restTemplate);
    }

    @PostConstruct
    public void setupConnectionProperties() {
        baseEhrUrl = environment.getProperty(EHR_DEMOGRAPHICS_BASE_URL);
        ehrUser = environment.getProperty(ConfigConstants.EHR_USERNAME);
        ehrPassword = environment.getProperty(ConfigConstants.EHR_PASSWORD);
    }
}
