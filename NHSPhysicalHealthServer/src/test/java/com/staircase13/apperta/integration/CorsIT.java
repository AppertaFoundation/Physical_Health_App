package com.staircase13.apperta.integration;

import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class CorsIT extends AbstractIntegrationTest {

    private static final String VALID_ORIGIN = "http://apperta-client-2";
    private static final String INVALID_ORIGIN = "http://apperta-client-4";

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private OAuthClient oAuthClient;

    @Test
    public void basicAuthNotRequiredForOPTIONS() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ORIGIN, VALID_ORIGIN);
        headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "POST");

        ResponseEntity<String> response = testRestTemplate()
                .exchange(
                        testUrlBuilder.createUrl("/oauth/token"),
                        HttpMethod.OPTIONS,
                        new HttpEntity<>(headers),
                        String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
    }

    @Test
    public void requestBlockedIfOriginWrong() throws IOException {

        HttpHeaders headers = oAuthClient.headersForClientCredentials();
        headers.add(HttpHeaders.ORIGIN, INVALID_ORIGIN);

        ResponseEntity<String> response = testRestTemplate()
                .exchange(
                        testUrlBuilder.createUrl("/api/ping"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.FORBIDDEN));
        assertThat(response.getBody(), is("Invalid CORS request"));
    }


    @Test
    public void requestAllowedIfOriginCorrect() throws IOException {

        HttpHeaders headers = oAuthClient.headersForClientCredentials();
        headers.add(HttpHeaders.ORIGIN, VALID_ORIGIN);

        ResponseEntity<String> response = testRestTemplate()
                .exchange(
                        testUrlBuilder.createUrl("/api/ping"),
                        HttpMethod.GET,
                        new HttpEntity<>(headers),
                        String.class);

        assertThat(response.getStatusCode(), is(HttpStatus.OK));
        assertThat(response.getBody(), is("pong"));
    }

}
