package com.staircase13.apperta.integration;

import com.staircase13.apperta.entities.DeviceNotification;
import com.staircase13.apperta.integration.util.TestUsers;
import com.staircase13.apperta.repository.DeviceNotificationRepository;
import com.staircase13.apperta.service.PushNotificationService;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.LocalDateTime;

import static com.staircase13.apperta.entities.NotificationState.SCHEDULED;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;

@TestPropertySource(properties = "apperta.notifications.schedule.cron=*/2 * * * * *")
public class NotificationsIT extends AbstractIntegrationTest {

    @Autowired
    private TestUsers testUsers;

    @Autowired
    private DeviceNotificationRepository notificationRepository;

    @Test
    public void pushNotificationsAreSent() {

        DeviceNotification notification = DeviceNotification.builder()
                .payload("This is my notification message")
                .state(SCHEDULED)
                .user(testUsers.getPatientUser())
                .sendTime(LocalDateTime.now().minusMinutes(1))
                .build();

        notificationRepository.save(notification);

        // TODO: temporary push service implementation that just stores messages in a static list
        Awaitility.waitAtMost(Duration.FIVE_SECONDS).until(() -> PushNotificationService.sentMessages, hasSize(1));
    }

}
