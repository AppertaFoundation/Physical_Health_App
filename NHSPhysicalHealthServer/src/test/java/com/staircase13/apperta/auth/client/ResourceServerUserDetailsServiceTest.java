package com.staircase13.apperta.auth.client;

import com.staircase13.apperta.entities.Role;
import com.staircase13.apperta.entities.User;
import com.staircase13.apperta.repository.AppertaUserRepository;
import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.when;

public class ResourceServerUserDetailsServiceTest {
    private static final String USERNAME = "theUsername";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private ResourceServerUserDetailsService service;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppertaUserRepository appertaUserRepository;

    @Test
    public void userExists_mapToAppertaPrincipal() {

        User user = User.builder()
                .username(USERNAME)
                .role(Role.PATIENT)
                .build();

        when(appertaUserRepository.findByUsername(USERNAME)).thenReturn(Optional.of(user));

        AppertaPrinciple userDetails = service.loadUserByUsername(USERNAME);

        assertThat(userDetails, notNullValue());
        assertThat(userDetails.getUsername(), Is.is(USERNAME));
        assertThat(userDetails.getPassword(), nullValue());

        Set<String> authorities = toStringSet(userDetails.getAuthorities());
        assertThat(authorities, hasSize(7));
        assertThat(authorities, hasItem("VIEW_USER_DETAILS"));
        assertThat(authorities, hasItem("GET_SET_DEVICE"));
        assertThat(authorities, hasItem("HCP_SEARCH"));
        assertThat(authorities, hasItem("PATIENT_UPDATE_PROFILE"));
        assertThat(authorities, hasItem("CREATE_EHR"));
        assertThat(authorities, hasItem("QUERY_EHR"));
        assertThat(authorities, hasItem("RESET_PASSWORD"));
    }

    @Test
    public void userDoesntExist_returnNull() {
        when(appertaUserRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());

        AppertaPrinciple userDetails = service.loadUserByUsername(USERNAME);
        assertThat(userDetails, nullValue());
    }

    private Set<String> toStringSet(Collection<? extends GrantedAuthority> authority) {
        return authority.stream().map(a -> a.getAuthority()).collect(Collectors.toSet());
    }

}
