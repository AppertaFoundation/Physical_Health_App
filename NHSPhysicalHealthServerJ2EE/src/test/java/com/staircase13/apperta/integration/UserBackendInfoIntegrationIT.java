package com.staircase13.apperta.integration;

import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import com.staircase13.apperta.service.dto.UserDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

public class UserBackendInfoIntegrationIT extends AbstractIntegrationTest {

    public static final String PATH = "/api/user/backendInfo?username=%s";

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private OAuthClient oAuthClient;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${apperta.backend.url}")
    private String backendUrl;

    private MockRestServiceServer mockBackendServer;

    @Before
    public void setUp() {
        mockBackendServer = MockRestServiceServer.bindTo(restTemplate).build();
    }

    @Test
    public void successPath() throws IOException {
//        mockBackendServer.expect(
//                requestTo(backendUrl + "/user?username=myUsername"))
//                .andRespond(withSuccess("{ \"k1\": \"v1\", \"k2\": \"v2\" }", MediaType.APPLICATION_JSON));
//
//        ResponseEntity<String> response = callEndpoint("myUsername");
//
//        assertThat(response.getStatusCode(), is(HttpStatus.OK));
//        assertThat(response.getBody(), is("{\"k1\":\"v1\",\"k2\":\"v2\"}"));
    }

    private ResponseEntity<String> callEndpoint(String username) throws IOException {
        HttpEntity<UserDto> entity = new HttpEntity<>(oAuthClient.headersForPatientUser());
        return testRestTemplate().exchange(
                getUrl(username),
                HttpMethod.GET, entity, String.class);
    }

    private String getUrl(String username) {
        return testUrlBuilder.createUrl(PATH, username);
    }

}