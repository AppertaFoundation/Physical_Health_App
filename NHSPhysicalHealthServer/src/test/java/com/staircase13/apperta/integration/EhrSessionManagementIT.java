package com.staircase13.apperta.integration;

import com.staircase13.apperta.ehrconnector.ConfigConstants;
import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto.EhrDemographicsArrayResponseDto;
import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto.EhrDemographicsDto;
import com.staircase13.apperta.integration.util.MockDemographicsServer;
import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import com.staircase13.apperta.integration.util.TestUsers;
import com.staircase13.apperta.service.dto.UserDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import static com.staircase13.apperta.ehrconnector.ConfigConstants.EHR_PASSWORD;
import static com.staircase13.apperta.ehrconnector.ConfigConstants.EHR_USERNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class EhrSessionManagementIT extends AbstractIntegrationTest {
    @Autowired
    private Environment environment;

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
    public void check_basic_authentication() throws Exception {
        expectPartyQuerySuccess();

        callAppertaGetProfile(HttpStatus.OK);

        mockDemographicsServer.verify();
    }

    private void expectPartyQuerySuccess() throws Exception {
        String ehrUser = environment.getProperty(ConfigConstants.EHR_USERNAME);
        String ehrPassword = environment.getProperty(ConfigConstants.EHR_PASSWORD);

        mockDemographicsServer.expectPartyQueryBasicAuth(
                TestUsers.PATIENT_USERNAME,
                EhrDemographicsArrayResponseDto
                        .builder()
                        .parties(Arrays.asList(EhrDemographicsDto
                                .builder()
                                .partyAdditionalInfo(Collections.emptyList())
                                .build()))
                        .build(),
                ehrUser,
                ehrPassword
        );
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
