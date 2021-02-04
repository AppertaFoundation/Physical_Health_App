package com.staircase13.apperta.repository;

import com.staircase13.apperta.entities.DeviceApp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeviceAppRepository extends JpaRepository<DeviceApp, Long> {

    Optional<DeviceApp> findByAppName(String appName);
}
