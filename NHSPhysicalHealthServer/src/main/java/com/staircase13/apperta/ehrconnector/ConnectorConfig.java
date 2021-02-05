package com.staircase13.apperta.ehrconnector;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
public class ConnectorConfig {

    // Switchable EHR Connectors

    @Configuration
    @ComponentScan("com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat")
    @ConditionalOnProperty(name = "apperta.ehr.connector", havingValue= "MarandFlat")
    static class MarandFlatConfig {
        // Deliberately empty
    }

    // Switchable Demographics Connectors

    @Configuration
    @ComponentScan("com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics")
    @ConditionalOnProperty(name = "apperta.ehrdemographics.connector", havingValue= "MarandDemographics")
    public class MarandDemographicsConfig {
        // deliberately empty
    }
}
