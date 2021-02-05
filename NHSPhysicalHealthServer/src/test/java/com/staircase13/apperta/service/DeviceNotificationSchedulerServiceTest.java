package com.staircase13.apperta.service;

import com.google.common.collect.Sets;
import com.staircase13.apperta.entities.User;
import com.staircase13.apperta.entities.DeviceNotification;
import com.staircase13.apperta.repository.DeviceNotificationRepository;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Optional;

import static com.staircase13.apperta.entities.NotificationState.SCHEDULED;
import static com.staircase13.apperta.entities.NotificationState.SENT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DeviceNotificationSchedulerServiceTest {

    private static final Long NOTIFICATION_1_ID = 1L;
    private static final String NOTIFICATION_1_MESSAGE = "Hello World!";
    private static final String TIME = "2018-03-12T10:29:59.00Z";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    private DeviceNotificationSchedulerService deviceNotificationSchedulerService;

    @Mock
    private PushNotificationService pushNotificationService;

    @Mock
    private DeviceNotificationRepository repository;

    private User notification1User;

    private DeviceNotification notification1Scheduled;

    private DeviceNotification notification1Sent;

    private Clock clock;

    private LocalDateTime now;

    @Before
    public void setupNotificationService() {
        clock = Clock.fixed(Instant.parse(TIME), ZoneId.systemDefault());
        now = LocalDateTime.now(clock);

        deviceNotificationSchedulerService = new DeviceNotificationSchedulerService(
                clock,
                repository,
                pushNotificationService,
                new MockTransactionTemplate());
    }

    @Before
    public void setupTestData() {
        notification1User = User.builder().emailAddress("user@localhost").build();
        notification1Scheduled = DeviceNotification.builder().id(NOTIFICATION_1_ID).user(notification1User).payload(NOTIFICATION_1_MESSAGE).state(SCHEDULED).build();
        notification1Sent = DeviceNotification.builder().id(NOTIFICATION_1_ID).user(notification1User).payload(NOTIFICATION_1_MESSAGE).state(SENT).build();
    }

    @Test
    public void sendNotifications_noneToSend() {
        when(repository.findByStateAndSendTimeLessThanEqual(SCHEDULED, now)).thenReturn(Collections.emptySet());

        deviceNotificationSchedulerService.sendNotifications();

        verifyZeroInteractions(pushNotificationService);
        verify(repository, times(0)).save(any());
    }

    @Test
    public void sendNotifications_success() {
        when(repository.findByStateAndSendTimeLessThanEqual(SCHEDULED,now)).thenReturn(Sets.newHashSet(notification1Scheduled));

        deviceNotificationSchedulerService.sendNotifications();

        verify(pushNotificationService).send(notification1User, NOTIFICATION_1_MESSAGE);
        verify(repository, times(1)).save(notification1Scheduled);

        assertThat(notification1Scheduled.getState(), is(SENT));
        assertThat(notification1Scheduled.getLastAction(), is(LocalDateTime.now(clock)));
    }

    private static class MockTransactionTemplate extends TransactionTemplate {
        @Override
        public <T> T execute(TransactionCallback<T> action) throws TransactionException {

            assertThat(getPropagationBehavior(), is(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

            return action.doInTransaction(mock(TransactionStatus.class));
        }
    }

}
