package com.staircase13.apperta.integration;

import com.staircase13.apperta.entities.Role;
import com.staircase13.apperta.integration.util.*;
import com.staircase13.apperta.service.dto.UserDto;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.IOException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

public class UserRegisterIntegrationIT extends AbstractIntegrationTest {

    public static final String PATH = "/api/user/register";

    @Autowired
    private TestUrlBuilder testUrlBuilder;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MockEhrServer mockEhrServer;

    @Autowired
    private OAuthClient oAuthClient;

    @Test
    public void register_successPath() throws IOException {
        UserTestDto userDto = UserTestDto.builder().username("myUserName").password("myPassword").emailAddress("me@localhost").role(Role.PATIENT).dateOfBirth("12/12/12").build();

        UserDto response = testRestTemplate().exchange(
                getRegisterUrl(),
                HttpMethod.POST,
                new HttpEntity<>(userDto,oAuthClient.headersForClientCredentials()),
                UserDto.class).getBody();

        Map<String,Object> persistedUser = jdbcTemplate.queryForMap("SELECT * from AppertaUser where username = ?", response.getUsername());
        assertThat(persistedUser.get("username"), is("myUserName"));
        assertThat(persistedUser.get("role"), is(Role.PATIENT.name()));
        assertThat(persistedUser.get("emailAddress"), is("me@localhost"));
    }

    @Test
    public void register_validationFailures() throws IOException {
        UserTestDto userWithoutPasswordDto = UserTestDto.builder().username("myUserName").password(null).emailAddress("me@localhost").role(Role.PATIENT).dateOfBirth("12/12/12").build();
        assertBadRegistrationRequest(userWithoutPasswordDto,"{\"field\":\"password\",\"message\":\"Must be at least 5 characters long and a mix of upper and lower case letters\"}");

        UserTestDto existingUsernameDto = UserTestDto.builder().username(TestUsers.HCP_1_USERNAME).password("myPassword").emailAddress("me@localhost").role(Role.PATIENT) .dateOfBirth("12/12/12").build();
        assertBadRegistrationRequest(existingUsernameDto,"apperta.user.username.already.registered: User with username '" + TestUsers.HCP_1_USERNAME + "' is already registered");
    }

    private void assertBadRegistrationRequest(UserTestDto userTestDto, String message) throws IOException {
        HttpEntity<UserTestDto> entity = new HttpEntity<>(userTestDto, oAuthClient.headersForClientCredentials());
        ResponseEntity<String> response = testRestTemplate().exchange(
                getRegisterUrl(),
                HttpMethod.POST,
                entity,
                String.class);
        assertThat(response.getStatusCode(), is(BAD_REQUEST));
        assertThat(response.getBody(), containsString(message));
    }

    private String getRegisterUrl() {
        return testUrlBuilder.createUrl(PATH);
    }
}
