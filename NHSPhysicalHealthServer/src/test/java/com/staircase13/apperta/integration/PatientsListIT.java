package com.staircase13.apperta.integration;

import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import com.staircase13.apperta.service.dto.UserDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class PatientsListIT extends AbstractIntegrationTest {
    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private OAuthClient oAuthClient;

    @Test
    public void ifNotAuthorised_Error()  {
        assertHttpResponsecode(oAuthClient.headersWithNoToken(), HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void ifHcpUser_Ok() throws IOException {
        assertHttpResponsecode(oAuthClient.headersForHcpUser(), HttpStatus.OK);
    }

    @Test
    public void ifPatientUser_Error() throws IOException {
        assertHttpResponsecode(oAuthClient.headersForResearchUser(), HttpStatus.FORBIDDEN);
    }

    @Test
    public void ifResearchUser_Error() throws IOException {
        assertHttpResponsecode(oAuthClient.headersForPatientUser(), HttpStatus.FORBIDDEN);
    }

    private void assertHttpResponsecode(HttpHeaders httpHeaders, HttpStatus expectedCode) {
        HttpEntity<UserDto> entity = new HttpEntity<>(httpHeaders);

        ResponseEntity<String> response = testRestTemplate().exchange(
                getUrl(),
                HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode(), is(expectedCode));
    }

    private String getUrl() {
        return testUrlBuilder
                .createBuilder()
                .pathSegment("api")
                .pathSegment("patients")
                .queryParam("start",1)
                .queryParam("pageSize",1)
                .build()
                .toUriString();
    }
}
