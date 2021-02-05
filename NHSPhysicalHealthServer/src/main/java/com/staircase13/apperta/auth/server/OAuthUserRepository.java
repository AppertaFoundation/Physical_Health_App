package com.staircase13.apperta.auth.server;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OAuthUserRepository extends JpaRepository<OAuthUser, Long> {
    Optional<OAuthUser> findByUsername(String username);
}
