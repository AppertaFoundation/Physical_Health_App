package com.staircase13.apperta.integration;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.staircase13.apperta.entities.Role;
import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import com.staircase13.apperta.integration.util.UserTestDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.io.IOException;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OAuthE2EIT extends AbstractIntegrationTest {

    private static final String USER_EMAIL = "test@here.com";
    private static final String USER_NAME = "newPatient";
    private static final String USER_PASSWORD = "newPatientPassword";
    private static final Role USER_ROLE = Role.PATIENT;
    private static final String USER_DOB = "12/12/12";

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private OAuthClient oAuthClient;

    @Test
    public void accessSecuredAPIWithOAuthToken() throws Exception {
        registerUser();

        TokenResponse tokenResponse = oAuthClient.retrievePasswordGrant(USER_NAME, USER_PASSWORD);

        ResponseEntity<String> response = callSecuredEndpoint(Optional.of(tokenResponse));

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));
    }

    @Test
    public void accessSecuredAPIWithoutOAuthToken()  {
        ResponseEntity<String> response = callSecuredEndpoint(Optional.empty());

        assertThat(response.getStatusCode(), is(HttpStatus.UNAUTHORIZED));
    }

    @Test
    public void useRefreshToken() throws Exception {
        registerUser();

        TokenResponse tokenResponse = oAuthClient.retrievePasswordGrant(USER_NAME, USER_PASSWORD);

        TokenResponse updatedResponse = oAuthClient.refreshToken(tokenResponse);

        ResponseEntity<String> response = callSecuredEndpoint(Optional.of(updatedResponse));

        assertThat(response.getStatusCode(), is(HttpStatus.NOT_FOUND));

    }

    private void registerUser() throws IOException {
        UserTestDto userDto = UserTestDto.builder()
                .username(USER_NAME)
                .password(USER_PASSWORD)
                .role(USER_ROLE)
                .emailAddress(USER_EMAIL)
                .dateOfBirth(USER_DOB)
                .build();

        testRestTemplate().exchange(
                getRegisterUrl(),
                HttpMethod.POST,
                new HttpEntity<>(userDto,oAuthClient.headersForClientCredentials()),
                UserTestDto.class);
    }

    private ResponseEntity<String> callSecuredEndpoint(Optional<TokenResponse> token) {

        HttpEntity<?> request = new HttpEntity<>(token.isPresent() ?  oAuthClient.headersForToken(token.get()) : new HttpHeaders());

        return testRestTemplate().exchange(
                getProfileUrl(),
                HttpMethod.GET, request, String.class);
    }

    private String getProfileUrl() {
        return testUrlBuilder.createUrl("/api/user/hcp/search?nhsId=test");
    }

    private String getRegisterUrl() {
        return testUrlBuilder.createUrl(UserRegisterIntegrationIT.PATH);
    }

}
