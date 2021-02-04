package com.staircase13.apperta.api;

import com.staircase13.apperta.auth.server.PasswordService;
import com.staircase13.apperta.service.dto.UserDto;
import com.staircase13.apperta.service.UserService;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Mockito.verify;

public class UserEndpointTest {

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private UserEndpoint userEndpoint;

    @Mock
    private UserService userService;

    @Mock
    private PasswordService passwordService;

    @Test
    public void register() throws Exception {
        UserDto userDto = new UserDto();
        userEndpoint.register(userDto);
        verify(userService).createUser(userDto);
    }

    @Test
    public void passwordResetTokenRequest() {
        PasswordResetTokenRequest request = PasswordResetTokenRequest.builder().username("myUserName").build();
        userEndpoint.passwordResetTokenRequest(request);
        verify(passwordService).issuePasswordResetToken("myUserName");
    }
}
