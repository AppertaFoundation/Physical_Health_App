package com.staircase13.apperta.auth.server;

import com.staircase13.apperta.entities.Role;
import lombok.Builder;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@Builder
public class AuthServerPrinciple implements UserDetails {
    private String username;
    private Collection<? extends GrantedAuthority> authorities;
    private String password;
    private boolean accountNonExpired;
    private boolean accountNonLocked;
    private boolean credentialsNonExpired;
    private boolean enabled;
}
