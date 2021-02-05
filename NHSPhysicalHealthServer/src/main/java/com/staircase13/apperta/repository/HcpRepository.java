package com.staircase13.apperta.repository;

import com.staircase13.apperta.entities.Hcp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HcpRepository extends JpaRepository<Hcp, Long> {
    Optional<Hcp> findByNhsId(String nhsId);
    Optional<Hcp> findByUserUsername(String username);
}
