package com.staircase13.apperta.integration;

import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNot.not;

public class HealthCheckIT extends AbstractIntegrationTest {

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private OAuthClient oAuthClient;

    @Test
    public void additionalDetailsShownForClientCredentialsToken() throws IOException {
        String response = callHealthCheckEndpoint(oAuthClient.headersForClientCredentials());

        assertThat(response, containsString("ehr"));
    }

    @Test
    public void additionalDetailsNotShownForNoToken() {
        String response = callHealthCheckEndpoint(new HttpHeaders());

        assertThat(response, not(containsString("ehr")));
    }

    @Test
    public void additionalDetailsNotShownForPasswordToken() throws IOException {
        String response = callHealthCheckEndpoint(oAuthClient.headersForPatientUser());

        assertThat(response, not(containsString("ehr")));
    }

    private String callHealthCheckEndpoint(HttpHeaders httpHeaders) {
        return testRestTemplate()
                .exchange(
                        testUrlBuilder.createUrl("/actuator/health"),
                        HttpMethod.GET,
                        new HttpEntity<>(httpHeaders),
                        String.class).getBody();
    }
}
