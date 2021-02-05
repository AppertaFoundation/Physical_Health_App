package com.staircase13.apperta.service;

import com.staircase13.apperta.auth.server.AuthServerUserDetailsService;
import com.staircase13.apperta.ehrconnector.ConfigConstants;
import com.staircase13.apperta.entities.User;
import com.staircase13.apperta.repository.AppertaUserRepository;
import com.staircase13.apperta.service.dto.UserDto;
import com.staircase13.apperta.service.exception.UsernameAlreadyRegisteredException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.core.env.Environment;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UserServiceTest {

    private static final String EMAIL_ADDRESS = "test@localhost";
    private static final String USER_NAME = "userName";
    private static final String PASSWORD = "thePassword";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @InjectMocks
    private UserService userService;

    @Mock
    private AppertaUserRepository appertaUserRepository;

    @Mock
    private EhrDemographicsService demographicsService;

    @Mock
    private Environment environment;

    @Mock
    private AuthServerUserDetailsService authServerUserDetailsService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Before
    public void setupRepository() {
        User persistedUser = new User();
        persistedUser.setUsername(USER_NAME);
        when(appertaUserRepository.save(any(User.class))).thenReturn(persistedUser);

        // Too late here for construction, needs to be run in a separate test
        when(environment.getProperty(ConfigConstants.PATIENT_COMMITTER_NAME)).thenReturn("PATIENT");
        when(environment.getProperty(ConfigConstants.PATIENT_COMMITTER_NUMBER)).thenReturn("1");
    }

    @Test
    public void createUser_dtoMappedToUserCorrectlyAndPasswordEncoded() throws Exception {
        when(appertaUserRepository.existsByUsername("username1")).thenReturn(false);

        UserDto userDto = UserDto.builder()
                .username(USER_NAME)
                .password(PASSWORD)
                .emailAddress(EMAIL_ADDRESS)
                .build();

        userService.createUser(userDto);

        verify(appertaUserRepository).save(userArgumentCaptor.capture());
        verify(authServerUserDetailsService).registerUser(USER_NAME,PASSWORD,EMAIL_ADDRESS);

        User persistedUser = userArgumentCaptor.getValue();
        assertThat(persistedUser, notNullValue());
        assertThat(persistedUser.getUsername(), is(USER_NAME));
    }

    @Test
    public void createUser_userInfoReturned() throws Exception {
        when(appertaUserRepository.existsByUsername(USER_NAME)).thenReturn(false);

        UserDto userDto = UserDto.builder().username(USER_NAME).password("password1").build();

        UserDto response = userService.createUser(userDto);

        assertThat(response.getUsername(), is(USER_NAME));
    }

    @Test
    public void createUser_ifUsernameAlreadyUsed_throwException() throws Exception {
        when(appertaUserRepository.existsByUsername(USER_NAME)).thenReturn(true);

        UserDto userDto = UserDto.builder().username(USER_NAME).password("password1").build();

        expectedException.expect(UsernameAlreadyRegisteredException.class);
        expectedException.expect(hasProperty("messageArgs",arrayContaining(USER_NAME)));

        userService.createUser(userDto);
    }

}
