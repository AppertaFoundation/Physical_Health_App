package com.staircase13.apperta.auth.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * This is called to authenticate users retrieving an OAuth Token from the
 * built in OAuth Server. It uses a separate users database table from the
 * main Apperta Application
 */
@Service
public class AuthServerUserDetailsService implements UserDetailsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthServerUserDetailsService.class);

    private final OAuthUserRepository oAuthUserRepository;

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthServerUserDetailsService(OAuthUserRepository oAuthUserRepository, PasswordEncoder passwordEncoder) {
        this.oAuthUserRepository = oAuthUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void registerUser(String username, String password, String emailAddress) {
        LOGGER.info("Registering OAuth Server User '{}'",username);

        OAuthUser user = new OAuthUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmailAddress(emailAddress);
        oAuthUserRepository.save(user);
    }

    @Override
    public AuthServerPrinciple loadUserByUsername(String username) throws UsernameNotFoundException {

        LOGGER.info("Load user by username '{}'",username);

        Optional<OAuthUser> optionalUser = oAuthUserRepository.findByUsername(username);

        if(!optionalUser.isPresent()) {
            throw new UsernameNotFoundException(String.format("Cannot find user with username '%s'", username));
        }

        OAuthUser user = optionalUser.get();

        return AuthServerPrinciple.builder()
                .username(username)
                .password(user.getPassword())
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .enabled(true)
                .build();
    }

}
