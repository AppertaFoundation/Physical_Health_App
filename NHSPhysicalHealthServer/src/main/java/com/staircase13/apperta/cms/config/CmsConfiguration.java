package com.staircase13.apperta.cms.config;

import com.staircase13.apperta.cms.NhsApiContentFactory;
import com.staircase13.apperta.cms.entities.NhsApi;
import com.staircase13.apperta.cms.loader.ApiLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class CmsConfiguration {
    public static final String API_LOADER_CONDITIONS = "conditionsApiLoader";
    public static final String API_LOADER_LIVEWELL = "livewellApiLoader";

    private final String subscriptionKey;

    private final String conditionsBaseUrl;

    private final String liveWellBaseUrl;

    private final NhsApiContentFactory nhsApiContentFactory;

    private final ApplicationContext applicationContext;

    @Autowired
    public CmsConfiguration(@Value("${apperta.cms.nhsapi.subscription.key}") String subscriptionKey,
                            @Value("${apperta.cms.nhsapi.conditions.baseurl}") String condtionsBaseUrl,
                            @Value("${apperta.cms.nhsapi.livewell.baseurl}") String liveWellBaseUrl,
                            NhsApiContentFactory nhsApiContentFactory, ApplicationContext applicationContext) {
        this.subscriptionKey = subscriptionKey;
        this.conditionsBaseUrl = condtionsBaseUrl;
        this.liveWellBaseUrl = liveWellBaseUrl;
        this.nhsApiContentFactory = nhsApiContentFactory;
        this.applicationContext = applicationContext;
    }

    @Bean(API_LOADER_CONDITIONS)
    public ApiLoader conditionsApiLoader() {
        return ApiLoader
                .builder()
                .restTemplate(applicationContext.getBean(RestTemplate.class))
                .subscriptionKey(subscriptionKey)
                .baseUrl(conditionsBaseUrl)
                .nhsApi(NhsApi.CONDITIONS)
                .nhsApiContentFactory(nhsApiContentFactory)
                .build();
    }

    @Bean(API_LOADER_LIVEWELL)
    public ApiLoader liveWellApiLoader() {
        return ApiLoader
                .builder()
                .restTemplate(applicationContext.getBean(RestTemplate.class))
                .subscriptionKey(subscriptionKey)
                .baseUrl(liveWellBaseUrl)
                .nhsApi(NhsApi.LIVE_WELL)
                .nhsApiContentFactory(nhsApiContentFactory)
                .build();
    }
}
