package com.staircase13.apperta.integration.util;

import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

public class MockServerFactory {
    public static MockRestServiceServer createMockServer(RestTemplate restTemplate) {
        return createMockServer(restTemplate,false);
    }

    public static MockRestServiceServer createMockServerIgnoreRequestOrder(RestTemplate restTemplate) {
        return createMockServer(restTemplate,true);
    }

    private static MockRestServiceServer createMockServer(RestTemplate restTemplate, boolean ignoreRequestOrder) {
        MockRestServiceServer mockServer = MockRestServiceServer.bindTo(restTemplate).ignoreExpectOrder(ignoreRequestOrder).build();
        // Ensures the RequestResponseLoggingInterceptor doesn't eat the body during tests
        // See https://github.com/spring-projects/spring-boot/issues/6686
        // Without this you'll always be returned with an empty response
        ClientHttpRequestFactory requestFactory = (ClientHttpRequestFactory) ReflectionTestUtils.getField(restTemplate, "requestFactory");
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(requestFactory));
        return mockServer;
    }

}
