package com.staircase13.apperta.integration.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.DemographicsConnector;
import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto.EhrDemographicsArrayResponseDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;

import javax.annotation.PostConstruct;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import static org.hamcrest.Matchers.startsWith;
import static org.hamcrest.core.IsInstanceOf.any;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@Component
public class MockDemographicsServer {

    @Autowired
    private DemographicsConnector demographicsConnector;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${apperta.ehrdemographics.baseurl}")
    private String ehrUrl;

    @Value("${apperta.ehr.user.username}")
    private String ehrUsername;

    @Value("${apperta.ehr.user.token}")
    private String ehrPassword;

    private MockRestServiceServer mockServer;

    @PostConstruct
    public void setUp() {
        mockServer = MockServerFactory.createMockServer(demographicsConnector.getUnderlyingRestTemplate());
    }

    public void reset() {
        mockServer.reset();
    }

    public void expectGetSession(String sessionId) {
        String userCheckUrl = ehrUrl + "/rest/v1/session?username=" + ehrUsername + "&password=" + ehrPassword;

        mockServer.expect(ExpectedCount.once(),
                requestTo(userCheckUrl))
                .andExpect(method(HttpMethod.POST))
                .andExpect(header(HttpHeaders.AUTHORIZATION, any(String.class)))
                .andRespond(withSuccess("{ \"action\": \"CREATE\", " +
                        " \"sessionId\": \"" + sessionId + "\" }", MediaType.APPLICATION_JSON));
    }

    public void expectRefreshSession(HttpStatus httpStatus) {
        String refreshUrl = ehrUrl + "/rest/v1/session";

        mockServer.expect(ExpectedCount.once(),
                requestTo(refreshUrl))
                .andExpect(method(HttpMethod.PUT))
                .andExpect(header(HttpHeaders.AUTHORIZATION, any(String.class)))
                .andRespond(withStatus(httpStatus));
    }

    public void expectDeleteSession(String sessionId, HttpStatus httpStatus) {
        String deleteUrl = ehrUrl + "/rest/v1/session";

        mockServer.expect(ExpectedCount.once(),
                requestTo(deleteUrl))
                .andExpect(method(HttpMethod.DELETE))
                .andExpect(header(HttpHeaders.AUTHORIZATION, any(String.class)))
                .andRespond(withStatus(httpStatus));
    }

    public void expectPartyQueryWithErrorResponse(String sessionId, String username, HttpStatus responseStatus) {
        String partyQueryUrl = ehrUrl + "/rest/v1/demographics/party/query?apperta.user=" + username;

        mockServer.expect(ExpectedCount.once(),
                requestTo(partyQueryUrl))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, any(String.class)))
                .andRespond(withStatus(responseStatus));
    }

    public void expectPartyQuery(String sessionId, String username, EhrDemographicsArrayResponseDto result) throws JsonProcessingException {
        String partyQueryUrl = ehrUrl + "/rest/v1/demographics/party/query?apperta.user=" + username;

        mockServer.expect(ExpectedCount.once(),
                requestTo(partyQueryUrl))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, any(String.class)))
                .andRespond(withSuccess(objectMapper.writeValueAsString(result), MediaType.APPLICATION_JSON));

    }

    public void expectPartyQueryBasicAuth(String username, EhrDemographicsArrayResponseDto result,
                                          String authUsername, String authPassword) throws JsonProcessingException {
        String partyQueryUrl = ehrUrl + "/rest/v1/demographics/party/query?apperta.user=" + username;

        Charset charset = StandardCharsets.ISO_8859_1;
        String credentialsString = authUsername + ":" + authPassword;
        byte[] encodedBytes = Base64.getEncoder().encode(credentialsString.getBytes(charset));
        String encodedCredentials = new String(encodedBytes, charset);

        mockServer.expect(ExpectedCount.once(),
                requestTo(partyQueryUrl))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, startsWith("Basic "+encodedCredentials)))
                .andRespond(withSuccess(objectMapper.writeValueAsString(result), MediaType.APPLICATION_JSON));

    }

    public void verify() {
        mockServer.verify();
    }
}
