package com.staircase13.apperta.integration;

import com.staircase13.apperta.api.PasswordResetRequest;
import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;

import static com.staircase13.apperta.integration.util.TestUsers.PATIENT_USERNAME;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class UserPasswordResetIT extends AbstractIntegrationTest {

    private static final String NEW_PASSWORD = "myNewPassword";

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private OAuthClient oAuthClient;

    @Test
    public void passwordReset_viaUI_successPath() throws Exception {

        PasswordResetRequest passwordResetRequest = new PasswordResetRequest();
        passwordResetRequest.setPassword(NEW_PASSWORD);

        HttpHeaders httpHeaders = oAuthClient.headersForPatientUser();
        HttpEntity<PasswordResetRequest> entity = new HttpEntity<>(passwordResetRequest,httpHeaders);

        ResponseEntity<String> response = testRestTemplate().exchange(
                getPasswordResetUrl(),
                HttpMethod.POST, entity, String.class);

        assertThat(response.getStatusCode(), Matchers.is(HttpStatus.OK));

        assertThat(oAuthClient.canRetrieveOauthToken(PATIENT_USERNAME, NEW_PASSWORD), is(true));

    }

    private String getPasswordResetUrl() {
        return testUrlBuilder.createUrl("/api/user/passwordReset");
    }

}
