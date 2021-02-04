package com.staircase13.apperta.auth.client;

import com.staircase13.apperta.entities.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Builder
public class AppertaPrinciple implements UserDetails {
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
    private Role role;

    // these properties aren't used in the resource server
    // but are required by the UserDetails contract
    private String password;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
}
