package com.staircase13.apperta.integration;

import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto.EhrDemographicsArrayResponseDto;
import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto.EhrDemographicsDto;
import com.staircase13.apperta.integration.util.MockDemographicsServer;
import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import com.staircase13.apperta.integration.util.TestUsers;
import com.staircase13.apperta.service.dto.UserDto;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EhrSessionManagementIT extends AbstractIntegrationTest {

    private static final String FIRST_SESSION_ID = "8116d917-4a4b-4543-aa95-9257d7aae17d";
    private static final String SECOND_SESSION_ID = "8116d917-4a4b-4543-aa95-9257d7aae17e";

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private OAuthClient oAuthClient;

    @Autowired
    private MockDemographicsServer mockDemographicsServer;

    @Before
    public void resetMocks() {
        mockDemographicsServer.reset();
    }

    @Test
    public void create_initial_session_and_use() throws Exception {
        // TODO: these session calls are only required for specific connectors
        //   mockDemographicsServer.expectGetSession(FIRST_SESSION_ID);
        expectPartyQuerySuccess(FIRST_SESSION_ID);

        callAppertaGetProfile(HttpStatus.OK);

        mockDemographicsServer.verify();
    }

    // TODO: these session calls are only required for specific connectors
//    @Test
//    public void existing_session_leads_to_403_token_refreshed() throws Exception {
//        mockDemographicsServer.expectGetSession(FIRST_SESSION_ID);
//        mockDemographicsServer.expectPartyQueryWithErrorResponse(FIRST_SESSION_ID, TestUsers.PATIENT_USERNAME, HttpStatus.FORBIDDEN);
//        mockDemographicsServer.expectRefreshSession(HttpStatus.OK);
//        expectPartyQuerySuccess(FIRST_SESSION_ID);
//
//        callAppertaGetProfile(HttpStatus.OK);
//
//        mockDemographicsServer.verify();
//    }

    // TODO: these session calls are only required for specific connectors
//    @Test
//    public void existing_session_leads_to_403_token_refresh_fails_get_new_token() throws Exception {
//
//        mockDemographicsServer.expectGetSession(FIRST_SESSION_ID);
//        mockDemographicsServer.expectPartyQueryWithErrorResponse(FIRST_SESSION_ID, TestUsers.PATIENT_USERNAME, HttpStatus.FORBIDDEN);
//        mockDemographicsServer.expectRefreshSession(HttpStatus.FORBIDDEN);
//        mockDemographicsServer.expectDeleteSession(FIRST_SESSION_ID, HttpStatus.OK);
//        mockDemographicsServer.expectGetSession(SECOND_SESSION_ID);
//        expectPartyQuerySuccess(SECOND_SESSION_ID);
//
//        callAppertaGetProfile(HttpStatus.OK);
//
//        mockDemographicsServer.verify();
//    }

//    @Test
//    public void existing_session_leads_to_403_token_refresh_fails_session_delete_fails_get_new_token() throws Exception {
//        mockDemographicsServer.expectGetSession(FIRST_SESSION_ID);
//        mockDemographicsServer.expectPartyQueryWithErrorResponse(FIRST_SESSION_ID, TestUsers.PATIENT_USERNAME, HttpStatus.FORBIDDEN);
//        mockDemographicsServer.expectRefreshSession(HttpStatus.FORBIDDEN);
//        mockDemographicsServer.expectDeleteSession(FIRST_SESSION_ID, HttpStatus.SERVICE_UNAVAILABLE);
//        mockDemographicsServer.expectGetSession(SECOND_SESSION_ID);
//        expectPartyQuerySuccess(SECOND_SESSION_ID);
//
//        callAppertaGetProfile(HttpStatus.OK);
//
//        mockDemographicsServer.verify();
//    }

    private void expectPartyQuerySuccess(String sessionId) throws Exception {
        mockDemographicsServer.expectPartyQuery(
                sessionId,
                TestUsers.PATIENT_USERNAME,
                EhrDemographicsArrayResponseDto
                        .builder()
                        .parties(Arrays.asList(EhrDemographicsDto
                                .builder()
                                .partyAdditionalInfo(Collections.emptyList())
                                .build()))
                        .build());
    }

    private void callAppertaGetProfile(HttpStatus expectedStatus) throws IOException {
        HttpEntity<UserDto> entity = new HttpEntity<>(oAuthClient.headersForPatientUser());

        String getProfileUrl = testUrlBuilder
                .createBuilder()
                .pathSegment("api")
                .pathSegment("user")
                .pathSegment("profile")
                .build()
                .toUriString();

        ResponseEntity<String> response = testRestTemplate().exchange(
                getProfileUrl,
                HttpMethod.GET, entity, String.class);

        assertThat(response.getStatusCode(), is(expectedStatus));
    }
}
