package com.staircase13.apperta.repository;

import com.staircase13.apperta.entities.User;
import com.staircase13.apperta.entities.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppertaUserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByUsernameAndRole(String username, Role role);

    boolean existsByUsername(String username1);
}
