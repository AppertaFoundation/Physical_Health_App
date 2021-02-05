package com.staircase13.apperta.integration;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.util.ServerSetupTest;
import com.staircase13.apperta.api.PasswordResetTokenRequest;
import com.staircase13.apperta.api.PasswordResetTokenVerifyRequest;
import com.staircase13.apperta.api.PasswordResetWithTokenRequest;
import com.staircase13.apperta.integration.util.OAuthClient;
import com.staircase13.apperta.integration.util.TestUrlBuilder;
import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static org.hamcrest.core.Is.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserPasswordResetWithTokenIT extends AbstractIntegrationTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserPasswordResetWithTokenIT.class);

    private static final String USER_NAME = "patient1";
    private static final String NEW_PASSWORD = "myNewPassword";

    private static final String URL_PASSWORD_RESET = "/iam/passwordReset";

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private OAuthClient oAuthClient;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @Test
    public void passwordReset_viaUI_successPath() throws Exception {
        callIssueResetTokenEndpoint();

        String token = extractTokenFromEmail();

        resetPasswordViaUi(token);

        assertThat(oAuthClient.canRetrieveOauthToken(USER_NAME, NEW_PASSWORD), is(true));
    }

    @Test
    public void passwordReset_viaUI_getInvalidToken_invalidTokenPageShown() throws Exception {
        this.mockMvc.perform(get(URL_PASSWORD_RESET)
                .param("token", "invalidToken"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("The link you clicked is invalid, expired, or has already been used")));
    }

    @Test
    public void passwordReset_viaUI_postInvalidToken_invalidTokenPageShown() throws Exception {
        this.mockMvc.perform(post(URL_PASSWORD_RESET)
                .param("resetToken", "invalidToken")
                .param("password", NEW_PASSWORD)
                .param("passwordRepeated", NEW_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("The link you clicked is invalid, expired, or has already been used")));
    }

    @Test
    public void passwordReset_viaAPI_successPath() throws Exception {
        callIssueResetTokenEndpoint();

        String token = extractTokenFromEmail();

        assertThat(callVerifyResetTokenEndpoint(token), is(true));

        callPasswordResetApiEndpoint(NEW_PASSWORD, token);

        assertThat(oAuthClient.canRetrieveOauthToken(USER_NAME, NEW_PASSWORD), is(true));
    }

    @Test
    public void passwordReset_viaAPI_invalidToken() throws IOException {
        ResponseEntity<String> response = callPasswordResetApiEndpoint("newPassword", "invalidtoken");
        assertThat(response.getStatusCode(), Matchers.is(BAD_REQUEST));
        assertThat(response.getBody(), Matchers.containsString("apperta.invalid.token: Token 'invalidtoken' is not valid, or has expired"));
    }

    private void callIssueResetTokenEndpoint() throws IOException{
        PasswordResetTokenRequest requestDto = new PasswordResetTokenRequest();
        requestDto.setUsername(USER_NAME);

        testRestTemplate().exchange(
                getPasswordResetTokenRequestUrl(),
                HttpMethod.POST,
                new HttpEntity<>(requestDto,oAuthClient.headersForClientCredentials()),
                Void.class);
    }

    private Boolean callVerifyResetTokenEndpoint(String token) throws IOException {
        PasswordResetTokenVerifyRequest requestDto = new PasswordResetTokenVerifyRequest();
        requestDto.setToken(token);

        return testRestTemplate().exchange(
                getPasswordResetTokenVerifyUrl(),
                HttpMethod.POST,
                new HttpEntity<>(requestDto,oAuthClient.headersForClientCredentials()),
                Boolean.class).getBody();
    }

    private ResponseEntity<String> callPasswordResetApiEndpoint(String password, String token) throws IOException {
        PasswordResetWithTokenRequest requestDto = new PasswordResetWithTokenRequest();
        requestDto.setPassword(password);
        requestDto.setToken(token);

        return testRestTemplate().exchange(
                getPasswordResetWithTokenUrl(),
                HttpMethod.POST,
                new HttpEntity<>(requestDto,oAuthClient.headersForClientCredentials()),
                String.class);
    }

    private String extractTokenFromEmail() throws IOException, MessagingException {
        MimeMessage[] emails = greenMail.getReceivedMessages();
        assertThat(emails, arrayWithSize(1));

        String emailContent = emails[0].getContent().toString();

        LOGGER.info("Email Content is '{}'",emailContent);

        Pattern urlPattern = Pattern.compile("token\\=[\\w\\-]+");
        Matcher urlMatcher = urlPattern.matcher(emailContent);
        urlMatcher.find();

        String token = emailContent.substring(urlMatcher.start(0) + "token=".length(),urlMatcher.end(0));

        return token;
    }

    private void resetPasswordViaUi(String token) throws Exception {
        LOGGER.info("Token is {}", token);

        this.mockMvc.perform(post(URL_PASSWORD_RESET)
                .param("resetToken", token)
                .param("password", NEW_PASSWORD)
                .param("passwordRepeated", NEW_PASSWORD))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Your password has been reset")));
    }

    private String getPasswordResetTokenRequestUrl() {
        return testUrlBuilder.createUrl("/api/user/passwordResetTokenRequest");
    }

    private String getPasswordResetTokenVerifyUrl() {
        return testUrlBuilder.createUrl("/api/user/passwordResetTokenVerify");
    }

    private String getPasswordResetWithTokenUrl() {
        return testUrlBuilder.createUrl("/api/user/passwordResetWithToken");
    }

}
