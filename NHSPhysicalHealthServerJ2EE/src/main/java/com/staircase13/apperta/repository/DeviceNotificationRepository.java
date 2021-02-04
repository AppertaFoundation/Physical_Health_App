package com.staircase13.apperta.repository;

import com.staircase13.apperta.entities.DeviceNotification;
import com.staircase13.apperta.entities.NotificationState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface DeviceNotificationRepository extends JpaRepository<DeviceNotification, Long> {
    Set<DeviceNotification> findByStateAndSendTimeLessThanEqual(NotificationState notificationState, LocalDateTime lessThan);

    List<DeviceNotification> findByUserUsername(String username);
}
