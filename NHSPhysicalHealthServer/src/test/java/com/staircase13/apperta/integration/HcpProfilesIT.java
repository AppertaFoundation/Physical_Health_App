package com.staircase13.apperta.integration;

import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import com.staircase13.apperta.service.dto.HcpDto;
import com.staircase13.apperta.service.dto.HcpSummaryDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import java.io.IOException;

import static com.staircase13.apperta.integration.util.TestUsers.HCP_1_USERNAME;
import static com.staircase13.apperta.integration.util.TestUsers.HCP_2_USERNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.http.HttpStatus.*;

public class HcpProfilesIT extends AbstractIntegrationTest {
    private static final String EMAIL_ADDRESS = "email@localhost";
    private static final String FIRST_NAME = "aFirstName";
    private static final String JOB_TITLE = "aJobTitle";
    private static final String JOB_TITLE_UPDATE = "aUpdatedJobTitle";
    private static final String LAST_NAME = "aLastName";
    private static final String LOCATION = "aLocation";
    private static final String NHS_ID = "aNhsId";
    private static final String TITLE = "aTitle";

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private OAuthClient oAuthClient;

    @Test
    public void getCreateAndUpdateHcpProfile_thenGetOverview() throws IOException {

        // Get HCP Information for a user with out HCP specific information
        ResponseEntity<HcpDto> getEmptyProfileResponse = getProfile();
        assertThat(getEmptyProfileResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(getEmptyProfileResponse.getBody().getUsername(), is(HCP_1_USERNAME));
        assertThat(getEmptyProfileResponse.getBody().getNhsId(), nullValue());

        HcpDto profileInformation = createValidHcpDto();

        // Populate HCP Information
        ResponseEntity<String> postNewProfileResponse = postProfile(profileInformation);
        assertThat(postNewProfileResponse.getStatusCode(), is(HttpStatus.OK));

        // Get Updated Information
        ResponseEntity<HcpDto> getUpdatedProfileResponse = getProfile();
        assertThat(getUpdatedProfileResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(getUpdatedProfileResponse.getBody().getUsername(), is(HCP_1_USERNAME));
        assertThat(getUpdatedProfileResponse.getBody().getEmail(), is(EMAIL_ADDRESS));
        assertThat(getUpdatedProfileResponse.getBody().getFirstNames(), is(FIRST_NAME));

        // Updated HCP Information
        profileInformation.setJobTitle(JOB_TITLE_UPDATE);
        ResponseEntity<String> updateHcpResponse = postProfile(profileInformation);
        assertThat(updateHcpResponse.getStatusCode(), is(HttpStatus.OK));

        // Do a Patient Search
        ResponseEntity<HcpSummaryDto> searchResponse = doSearch(NHS_ID);
        assertThat(searchResponse.getStatusCode(), is(HttpStatus.OK));
        assertThat(searchResponse.getBody().getJobTitle(), is(JOB_TITLE_UPDATE));
    }

    @Test
    public void createHcp_invalidRequestScenarios() throws IOException {
        HcpDto profileInformation = createValidHcpDto();

        profileInformation.setNhsId(null);
        ResponseEntity<String> missingFieldsResponse = postProfile(profileInformation);
        assertThat(missingFieldsResponse.getStatusCode(), is(BAD_REQUEST));
        assertThat(missingFieldsResponse.getBody(), containsString("must not be empty"));

        profileInformation.setUsername("notTheLoggedInUser");
        profileInformation.setNhsId(NHS_ID);
        ResponseEntity<String> incorrectUsernameResponse = postProfile(profileInformation);
        assertThat(incorrectUsernameResponse.getStatusCode(), is(BAD_REQUEST));
        assertThat(incorrectUsernameResponse.getBody(), containsString("apperta.hcp.incorrect.username: An attempt was made to update HCP with username 'notTheLoggedInUser' but that doesn't match the authenticated username 'hcp1'"));

        profileInformation.setUsername(HCP_1_USERNAME);
        ResponseEntity<String> profilePostedResponse = postProfile(profileInformation);
        assertThat(profilePostedResponse.getStatusCode(), is(OK));

        profileInformation.setUsername(HCP_2_USERNAME);
        profileInformation.setNhsId(NHS_ID);
        ResponseEntity<String> nhsIdAlreadyUsedResponse = postProfile(profileInformation, oAuthClient.headersForOtherHcpUser());
        assertThat(nhsIdAlreadyUsedResponse.getStatusCode(), is(BAD_REQUEST));
        assertThat(nhsIdAlreadyUsedResponse.getBody(), containsString("apperta.hcp.nhsid.already.registered: NHS ID 'aNhsId' has already been registered"));
    }

    @Test
    public void search_ifNotAuthorised_Error()  {
        assertHttpResponseCodeSearch(oAuthClient.headersWithNoToken(), UNAUTHORIZED);
    }

    @Test
    public void search_ifHcpUser_Error() throws IOException {
        assertHttpResponseCodeSearch(oAuthClient.headersForHcpUser(), FORBIDDEN);
    }

    @Test
    public void search_ifResearchUser_Error() throws IOException {
        assertHttpResponseCodeSearch(oAuthClient.headersForResearchUser(), FORBIDDEN);
    }

    @Test
    public void getProfile_ifNotAuthorised_Error()  {
        assertHttpResponseCodeGetProfile(oAuthClient.headersWithNoToken(), UNAUTHORIZED);
    }

    @Test
    public void getProfile_ifPatientUser_Error() throws IOException {
        assertHttpResponseCodeGetProfile(oAuthClient.headersForPatientUser(), FORBIDDEN);
    }

    @Test
    public void getProfile_ifResearchUser_Error() throws IOException {
        assertHttpResponseCodeGetProfile(oAuthClient.headersForResearchUser(), FORBIDDEN);
    }

    @Test
    public void postProfile_ifNotAuthorised_Error()  {
        assertHttpResponseCodePostProfile(oAuthClient.headersWithNoToken(), UNAUTHORIZED);
    }

    @Test
    public void postProfile_ifPatientUser_Error() throws IOException {
        assertHttpResponseCodePostProfile(oAuthClient.headersForPatientUser(), FORBIDDEN);
    }

    @Test
    public void postProfile_ifResearchUser_Error() throws IOException {
        assertHttpResponseCodePostProfile(oAuthClient.headersForResearchUser(), FORBIDDEN);
    }

    private ResponseEntity<String> postProfile(HcpDto profileInformation) throws IOException {
        return postProfile(profileInformation, oAuthClient.headersForHcpUser());
    }

    private ResponseEntity<String> postProfile(HcpDto profileInformation, HttpHeaders headers) {
        return testRestTemplate().exchange(
                postProfileUrl(),
                HttpMethod.POST,
                new HttpEntity<>(profileInformation,headers),
                String.class);
    }

    private ResponseEntity<HcpDto> getProfile() throws IOException {
        return testRestTemplate().exchange(
                getProfileUrl(),
                HttpMethod.GET,
                new HttpEntity<>(oAuthClient.headersForHcpUser()),
                HcpDto.class);
    }

    private void assertHttpResponseCodeSearch(HttpHeaders httpHeaders, HttpStatus expectedCode) {
        ResponseEntity<String> response = testRestTemplate().exchange(
                searchUrl("NHS001"),
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                String.class);

        assertThat(response.getStatusCode(), is(expectedCode));
    }

    private void assertHttpResponseCodeGetProfile(HttpHeaders httpHeaders, HttpStatus expectedCode) {
        ResponseEntity<String> response = testRestTemplate().exchange(
                getProfileUrl(),
                HttpMethod.GET,
                new HttpEntity<>(httpHeaders),
                String.class);

        assertThat(response.getStatusCode(), is(expectedCode));
    }

    private void assertHttpResponseCodePostProfile(HttpHeaders httpHeaders, HttpStatus expectedCode) {
        ResponseEntity<String> response = testRestTemplate().exchange(
                postProfileUrl(),
                HttpMethod.POST,
                new HttpEntity<>(createValidHcpDto(), httpHeaders),
                String.class);

        assertThat(response.getStatusCode(), is(expectedCode));
    }

    private ResponseEntity<HcpSummaryDto> doSearch(String nhsId) throws IOException {
        return testRestTemplate()
                .exchange(
                        searchUrl(nhsId),
                        HttpMethod.GET,
                        new HttpEntity<>(oAuthClient.headersForPatientUser()),
                        HcpSummaryDto.class);
    }

    private HcpDto createValidHcpDto() {
        return HcpDto.builder()
                .username(HCP_1_USERNAME)
                .email(EMAIL_ADDRESS)
                .firstNames(FIRST_NAME)
                .jobTitle(JOB_TITLE)
                .lastName(LAST_NAME)
                .location(LOCATION)
                .nhsId(NHS_ID)
                .title(TITLE).build();
    }

    private String getProfileUrl() {
        return testUrlBuilder
                .createBuilder()
                .pathSegment("api")
                .pathSegment("user")
                .pathSegment("hcp")
                .pathSegment("profile")
                .build()
                .toUriString();
    }

    private String searchUrl(String nhsId) {
        return testUrlBuilder
                .createBuilder()
                .pathSegment("api")
                .pathSegment("user")
                .pathSegment("hcp")
                .pathSegment("search")
                .queryParam("nhsId",nhsId)
                .build()
                .toUriString();
    }

    private String postProfileUrl() {
        return getProfileUrl();
    }

}
