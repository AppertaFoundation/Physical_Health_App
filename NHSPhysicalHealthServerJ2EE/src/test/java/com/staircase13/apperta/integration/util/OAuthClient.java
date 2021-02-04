package com.staircase13.apperta.integration.util;

import com.google.api.client.auth.oauth2.*;
import com.google.api.client.http.BasicAuthentication;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;

import static com.staircase13.apperta.integration.util.TestUsers.*;

@Component
public class OAuthClient {
    private static final Logger LOG = LoggerFactory.getLogger(OAuthClient.class);

    private static final String PATH = "/oauth/token";
    private static final String CLIENT_ID = "test-client";
    private static final String CLIENT_SECRET = "test-password";

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    public TokenResponse retrieveClientCredentialsGrant() throws IOException {
        TokenResponse tokenResponse =
                new ClientCredentialsTokenRequest(
                        new NetHttpTransport(),
                        new JacksonFactory(),
                        new GenericUrl(testUrlBuilder.createUrl(PATH)))
                        .setClientAuthentication(
                                new BasicAuthentication(CLIENT_ID, CLIENT_SECRET)).execute();

        LOG.info("Client Credentials Grant token is '{}'",tokenResponse.getAccessToken());
        return tokenResponse;
    }

    public TokenResponse retrievePasswordGrant(String username, String password) throws IOException {
        TokenResponse tokenResponse =
                new PasswordTokenRequest(
                        new NetHttpTransport(),
                        new JacksonFactory(),
                        new GenericUrl(testUrlBuilder.createUrl(PATH)), username, password)
                        .setClientAuthentication(
                                new BasicAuthentication(CLIENT_ID, CLIENT_SECRET)).execute();

        LOG.info("Retrieve Password Grant token is '{}'",tokenResponse.getAccessToken());
        return tokenResponse;
    }

    public TokenResponse refreshToken(TokenResponse originalResponse) throws IOException {

        String refreshToken = originalResponse.getRefreshToken();

        LOG.info("Using Refresh Token '{}'",refreshToken);

        TokenResponse refreshResponse = new RefreshTokenRequest(
                new NetHttpTransport(),
                new JacksonFactory(),
                new GenericUrl(testUrlBuilder.createUrl(PATH)), refreshToken)
                .setClientAuthentication(
                        new BasicAuthentication(CLIENT_ID, CLIENT_SECRET)).execute();

        return refreshResponse;
    }

    public boolean canRetrieveOauthToken(String username, String password) throws IOException {
        try {
            retrievePasswordGrant(username, password);
            return true;
        } catch(TokenResponseException e) {
            e.printStackTrace();
            return false;
        }
    }

    public HttpHeaders headersWithNoToken() {
        return new HttpHeaders();
    }

    public HttpHeaders headersForToken(TokenResponse tokenResponse) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, String.format("Bearer %s", tokenResponse.getAccessToken()));
        return headers;
    }

    public HttpHeaders headersForClientCredentials() throws IOException {
        return headersForToken(retrieveClientCredentialsGrant());
    }

    public HttpHeaders headersForPatientUser() throws IOException {
        return headersForToken(retrievePasswordGrant(PATIENT_USERNAME,PATIENT_PASSWORD));
    }

    public HttpHeaders headersForResearchUser() throws IOException {
        return headersForToken(retrievePasswordGrant(RESEARCH_USERNAME,RESEARCH_PASSWORD));
    }

    public HttpHeaders headersForHcpUser() throws IOException {
        return headersForToken(retrievePasswordGrant(HCP_1_USERNAME, HCP_1_PASSWORD));
    }

    public HttpHeaders headersForOtherHcpUser() throws IOException {
        return headersForToken(retrievePasswordGrant(HCP_2_USERNAME, HCP_2_PASSWORD));
    }
}
