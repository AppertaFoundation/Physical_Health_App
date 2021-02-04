package com.staircase13.apperta.repository;

import com.staircase13.apperta.auth.server.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {

}
