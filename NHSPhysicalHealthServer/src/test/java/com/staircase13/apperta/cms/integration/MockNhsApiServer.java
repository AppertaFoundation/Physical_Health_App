package com.staircase13.apperta.cms.integration;

import com.staircase13.apperta.cms.config.CmsConfiguration;
import com.staircase13.apperta.cms.loader.ApiLoader;
import com.staircase13.apperta.integration.util.MockServerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import javax.annotation.PostConstruct;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@Lazy
@Component
public class MockNhsApiServer {

    @Autowired
    @Qualifier(CmsConfiguration.API_LOADER_CONDITIONS)
    private ApiLoader conditionsApiLoader;

    @Autowired
    @Qualifier(CmsConfiguration.API_LOADER_LIVEWELL)
    private ApiLoader liveWellApiLoader;

    @Value("${apperta.cms.nhsapi.conditions.baseurl}")
    private String conditionsBaseUrl;

    @Value("${apperta.cms.nhsapi.livewell.baseurl}")
    private String liveWellBaseUrl;

    private MockRestServiceServer conditionsMockServer;
    private MockRestServiceServer liveWellMockServer;

    @PostConstruct
    public void setUp() {
        conditionsMockServer = MockServerFactory.createMockServerIgnoreRequestOrder(conditionsApiLoader.getRestTemplate());
        liveWellMockServer = MockServerFactory.createMockServerIgnoreRequestOrder(liveWellApiLoader.getRestTemplate());
    }

    public void addMockConditionContent(String topic, String json) {
        conditionsMockServer.expect(ExpectedCount.between(0,Integer.MAX_VALUE),
                requestTo(conditionsBaseUrl + "/" + topic))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    public void addMockConditionContent(String topic, String page, String json) {
        conditionsMockServer.expect(ExpectedCount.between(0,Integer.MAX_VALUE),
                requestTo(conditionsBaseUrl + "/" + topic + "/" + page))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    public void addMockLiveWellContent(String topic, String json) {
        liveWellMockServer.expect(ExpectedCount.between(0,Integer.MAX_VALUE),
                requestTo(liveWellBaseUrl + "/" + topic))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    public void addMockLiveWellContent(String topic, String page, String json) {
        liveWellMockServer.expect(ExpectedCount.between(0,Integer.MAX_VALUE),
                requestTo(liveWellBaseUrl + "/" + topic + "/" + page))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));
    }

    public void reset() {
        conditionsMockServer.reset();
        liveWellMockServer.reset();
    }

    public void refuseAllFutureRequests() {
        reset();
    }
}
