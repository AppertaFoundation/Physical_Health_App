package com.staircase13.apperta.auth.client;

import com.staircase13.apperta.entities.User;
import com.staircase13.apperta.repository.AppertaUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Given a username on an OAuth Token, this user details service builds
 * an Apperta Principle, including any privileges that apply for the
 * user's role.
 */
@Service
public class ResourceServerUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceServerUserDetailsService.class);

    private final AppertaUserRepository appertaUserRepository;

    @Autowired
    public ResourceServerUserDetailsService(AppertaUserRepository appertaUserRepository) {
        this.appertaUserRepository = appertaUserRepository;
    }

    @Override
    public AppertaPrinciple loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.info("Load user by username '{}', if if exists locally",username);

        Optional<User> optionalUser = appertaUserRepository.findByUsername(username);

        if(!optionalUser.isPresent()) {
            // This indicates that we have been provided an authorisation token for
            // a user we're not aware of. In this case Spring will wire in a JWT principle
            // instead of the AppertaPrinciple
            return null;
        }

        User user = optionalUser.get();

        Collection<? extends GrantedAuthority> authorities = buildAuthorities(user);
        LOGGER.debug("Have extracted authorities [{}]",authorities);

        return AppertaPrinciple.builder()
                .authorities(authorities)
                .username(username)
                .role(user.getRole())
                .build();
    }


    private Collection<? extends GrantedAuthority> buildAuthorities(User user) {
        return user.getRole().getAuthorities()
                .stream()
                .map(p -> new SimpleGrantedAuthority(p.name()))
                .collect(Collectors.toList());
    }
}
