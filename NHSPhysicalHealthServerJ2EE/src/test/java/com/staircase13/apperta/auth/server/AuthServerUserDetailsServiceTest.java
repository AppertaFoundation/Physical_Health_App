package com.staircase13.apperta.auth.server;

import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthServerUserDetailsServiceTest {

    private static final String EMAIL = "test@localhost";
    private static final String USERNAME = "theUsername";
    private static final String PASSWORD = "thePassword";
    private static final String PASSWORD_ENCRYPTED = "theEncryptedPassword";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private AuthServerUserDetailsService service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private OAuthUserRepository oAuthUserRepository;

    @Captor
    private ArgumentCaptor<OAuthUser> oAuthUserArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void registerUser() {
        when(passwordEncoder.encode(PASSWORD)).thenReturn(PASSWORD_ENCRYPTED);

        service.registerUser(USERNAME,PASSWORD,EMAIL);

        verify(oAuthUserRepository).save(oAuthUserArgumentCaptor.capture());

        OAuthUser oAuthUser = oAuthUserArgumentCaptor.getValue();
        assertThat(oAuthUser.getUsername(), is(USERNAME));
        assertThat(oAuthUser.getPassword(), is(PASSWORD_ENCRYPTED));
        assertThat(oAuthUser.getEmailAddress(), is(EMAIL));
    }


    @Test
    public void userExists_mapToSpringAuthServerPrinciple() {

        OAuthUser oAuthUser = OAuthUser.builder()
                .username("myUser")
                .password("myPassword")
                //.role(Role.PATIENT)
                .build();

        when(oAuthUserRepository.findByUsername("myUser")).thenReturn(Optional.of(oAuthUser));

        AuthServerPrinciple userDetails = service.loadUserByUsername("myUser");

        assertThat(userDetails, notNullValue());
        assertThat(userDetails.getUsername(), Is.is("myUser"));
        assertThat(userDetails.getPassword(), Is.is("myPassword"));
        assertThat(userDetails.isAccountNonExpired(), Is.is(true));
        assertThat(userDetails.isAccountNonLocked(), Is.is(true));
        assertThat(userDetails.isCredentialsNonExpired(), Is.is(true));
        assertThat(userDetails.isEnabled(), Is.is(true));
    }


    @Test
    public void userDoesntExist_throwException() {
        exception.expect(UsernameNotFoundException.class);
        exception.expectMessage("Cannot find user with username 'myUser'");

        when(oAuthUserRepository.findByUsername("myUser")).thenReturn(Optional.empty());

        service.loadUserByUsername("myUser");
    }

}
