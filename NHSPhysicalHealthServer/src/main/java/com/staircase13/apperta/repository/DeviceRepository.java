package com.staircase13.apperta.repository;

import com.staircase13.apperta.entities.Device;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceRepository extends JpaRepository<Device, Long> {

    Optional<Device> findByUuid(String uuid);

    List<Device> findByUsername(String username);

    List<Device> findByUsernameStartingWith(String partialUsername, Pageable pageable);
}
