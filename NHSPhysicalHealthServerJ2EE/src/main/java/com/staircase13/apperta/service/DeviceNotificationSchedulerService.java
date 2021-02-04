package com.staircase13.apperta.service;

import com.staircase13.apperta.entities.DeviceNotification;
import com.staircase13.apperta.entities.NotificationState;
import com.staircase13.apperta.repository.DeviceNotificationRepository;
import net.javacrumbs.shedlock.core.SchedulerLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class DeviceNotificationSchedulerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceNotificationSchedulerService.class);

    private final Clock clock;

    private final DeviceNotificationRepository deviceNotificationRepository;

    private final PushNotificationService pushNotificationService;

    private final TransactionTemplate transactionTemplateRequiresNew;

    @Autowired
    public DeviceNotificationSchedulerService(Clock clock, DeviceNotificationRepository deviceNotificationRepository, PushNotificationService pushNotificationService, TransactionTemplate transactionTemplate) {
        this.clock = clock;
        this.deviceNotificationRepository = deviceNotificationRepository;
        this.pushNotificationService = pushNotificationService;
        this.transactionTemplateRequiresNew = transactionTemplate;

        transactionTemplateRequiresNew.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    @Scheduled(cron = "${apperta.notifications.schedule.cron}")
    @SchedulerLock(name = "sendDeviceNotifications")
    public void sendNotifications() {
        Set<DeviceNotification> unsentNotifications = deviceNotificationRepository.findByStateAndSendTimeLessThanEqual(NotificationState.SCHEDULED, LocalDateTime.now(clock));

        LOGGER.trace("'{}' notifications to send",unsentNotifications.size());

        for(DeviceNotification unsentNotification : unsentNotifications) {
            sendNotification(unsentNotification);
        }
    }

    private void sendNotification(DeviceNotification notification) {
        LOGGER.debug("Sending notification for user '{}' with message '{}'", notification.getUser().getUsername(), notification.getPayload());

        pushNotificationService.send(notification.getUser(), notification.getPayload());
        notification.setState(NotificationState.SENT);
        notification.setLastAction(LocalDateTime.now(clock));

        deviceNotificationRepository.save(notification);
    }
}
