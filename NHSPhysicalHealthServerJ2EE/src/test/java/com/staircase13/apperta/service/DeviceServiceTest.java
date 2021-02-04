package com.staircase13.apperta.service;

import com.staircase13.apperta.entities.*;
import com.staircase13.apperta.repository.DeviceNotificationRepository;
import com.staircase13.apperta.repository.DeviceRepository;
import com.staircase13.apperta.service.dto.DeviceDto;
import com.staircase13.apperta.service.dto.DeviceNotificationDto;
import com.staircase13.apperta.repository.AppertaUserRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.staircase13.apperta.entities.DeviceType.ANDROID;
import static com.staircase13.apperta.service.util.DateTimeUtil.toEpoch;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class DeviceServiceTest {

    private static final String PERSISTED_USER_NAME1 = "Username1";
    private static final String PERSISTED_USER_NAME2 = "Username2";
    private static final Long PERSISTED_DEVICE_ID1 = 235L;
    private static final Long PERSISTED_DEVICE_ID2 = 467L;
    private static final String PERSISTED_DEVICE_UUID1 =  "Uuid1";
    private static final String PERSISTED_DEVICE_UUID2 =  "Uuid2";
    private static final String PERSISTED_TOKEN1 = "TokenToken1";
    private static final DeviceType PERSISTED_DEVICE_TYPE = ANDROID;
    private static final NotificationState PERSISTED_STATE = NotificationState.SCHEDULED;
    private static final LocalDateTime NOTIFICATION_TIME = LocalDateTime.now().withNano(0);

    private static final User PERSISTED_APPERTA_USER_1 = User.builder().username(PERSISTED_USER_NAME1).build();
    private static final User PERSISTED_APPERTA_USER_2 = User.builder().username(PERSISTED_USER_NAME2).build();

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private DeviceService deviceService;

    @Mock
    private DeviceRepository deviceRepository;

    @Mock
    private AppertaUserRepository appertaUserRepository;

    @Mock
    private DeviceNotificationRepository notificationRepository;

    @Captor
    private ArgumentCaptor<Device> deviceArgumentCaptor;

    @Captor
    private ArgumentCaptor<DeviceNotification> notificationArgumentCaptor;

    private Device persistedDevice;
    private DeviceDto firstDeviceDto;

    private DeviceNotification persistedNotification;
    private DeviceNotificationDto firstNotificationDto;

    @Before
    public void setupRepository() {
        persistedDevice = Device.builder().id(PERSISTED_DEVICE_ID1).uuid(PERSISTED_DEVICE_UUID1).username(PERSISTED_USER_NAME1)
                .deviceType(PERSISTED_DEVICE_TYPE).token(PERSISTED_TOKEN1).build();
        firstDeviceDto = DeviceDto.builder().id(PERSISTED_DEVICE_ID1).uuid(PERSISTED_DEVICE_UUID1)
                .type(PERSISTED_DEVICE_TYPE).token(PERSISTED_TOKEN1).build();

        persistedNotification = DeviceNotification.builder().id(PERSISTED_DEVICE_ID1).user(PERSISTED_APPERTA_USER_1)
                .state(PERSISTED_STATE).sendTime(NOTIFICATION_TIME).lastAction(NOTIFICATION_TIME).payload(PERSISTED_TOKEN1).build();
        firstNotificationDto = DeviceNotificationDto.builder().id(PERSISTED_DEVICE_ID1).state(PERSISTED_STATE)
                .sendTime(toEpoch(NOTIFICATION_TIME)).payload(PERSISTED_TOKEN1).build();

        when(appertaUserRepository.findByUsername(PERSISTED_USER_NAME1)).thenReturn(Optional.of(PERSISTED_APPERTA_USER_1));
        when(appertaUserRepository.findByUsername(PERSISTED_USER_NAME2)).thenReturn(Optional.of(PERSISTED_APPERTA_USER_2));
    }

    @Test
    public void findDeviceByUuid_mappedToDeviceDtoCorrectly() {
        when(deviceRepository.findByUuid(PERSISTED_DEVICE_UUID1)).thenReturn(Optional.of(persistedDevice));

        Optional<DeviceDto> deviceDtoOpt = deviceService.findDeviceByUuid(PERSISTED_DEVICE_UUID1);

        assert deviceDtoOpt.isPresent();
        DeviceDto dto = deviceDtoOpt.get();
        assertThat(dto.getId(), is(PERSISTED_DEVICE_ID1));
        assertThat(dto.getUuid(), is(PERSISTED_DEVICE_UUID1));
        assertThat(dto.getToken(), is(PERSISTED_TOKEN1));
        assertThat(dto.getType(), is(PERSISTED_DEVICE_TYPE));
    }

    @Test
    public void findDeviceByUuid_failMissing() {
        Optional<DeviceDto> deviceDtoOpt = deviceService.findDeviceByUuid(PERSISTED_DEVICE_UUID2);
        assert !deviceDtoOpt.isPresent();
    }

    @Test
    public void findDeviceForUsername_mappedToDeviceDtoCorrectly() {
        when(deviceRepository.findByUsername(PERSISTED_USER_NAME1)).thenReturn(Collections.singletonList(persistedDevice));

        List<DeviceDto> deviceDtoList = deviceService.findDevicesForUsername(PERSISTED_USER_NAME1);

        assertThat(deviceDtoList.size(), is(1));
        DeviceDto dto = deviceDtoList.get(0);
        assertThat(dto.getId(), is(PERSISTED_DEVICE_ID1));
        assertThat(dto.getUuid(), is(PERSISTED_DEVICE_UUID1));
        assertThat(dto.getToken(), is(PERSISTED_TOKEN1));
        assertThat(dto.getType(), is(PERSISTED_DEVICE_TYPE));
    }

    @Test
    public void findDeviceForUsername_noDevices() {
        List<DeviceDto> deviceDtoList = deviceService.findDevicesForUsername(PERSISTED_USER_NAME2);
        assertThat(deviceDtoList.size(), is(0));
    }

    @Test
    public void updateOrCreateDevice_createNewDevice() {
        when(deviceRepository.save(any(Device.class))).thenReturn(persistedDevice);

        DeviceDto secondDeviceDto = DeviceDto.builder().id(PERSISTED_DEVICE_ID2).uuid(PERSISTED_DEVICE_UUID2)
                .type(PERSISTED_DEVICE_TYPE).token(PERSISTED_TOKEN1).build();
        DeviceDto result = deviceService.updateOrCreateDevice(secondDeviceDto, PERSISTED_USER_NAME1);

        verify(deviceRepository).save(deviceArgumentCaptor.capture());
        Device value = deviceArgumentCaptor.getValue();
        assertThat(value.getId(), is(PERSISTED_DEVICE_ID2));
        assertThat(value.getUuid(), is(PERSISTED_DEVICE_UUID2));
        assertThat(value.getToken(), is(PERSISTED_TOKEN1));
        assertThat(value.getDeviceType(), is(PERSISTED_DEVICE_TYPE));
        assertThat(value.getUsername(), is(PERSISTED_USER_NAME1));

        assertThat(result.getId(), is(PERSISTED_DEVICE_ID1));
        assertThat(result.getUuid(), is(PERSISTED_DEVICE_UUID1));
        assertThat(result.getToken(), is(PERSISTED_TOKEN1));
        assertThat(result.getType(), is(PERSISTED_DEVICE_TYPE));
    }

    @Test
    public void updateOrCreateDevice_failDuplicateUuidCreation() {
// TODO        when(deviceRepository.save(any(Device.class))).thenThrow()
    }

    @Test
    public void updateOrCreateDevice_failUpdateDeviceNotUsers() {
        when(deviceRepository.findById(PERSISTED_DEVICE_ID1)).thenReturn(Optional.of(persistedDevice));
        // TODO: fix
        boolean threw = false;
        try {
            deviceService.updateOrCreateDevice(firstDeviceDto, PERSISTED_USER_NAME2);
        } catch (RuntimeException rte) {
            threw = true;
        }
        assert threw;
    }

    @Test
    public void deleteDevice_deleteOk() {
        when(deviceRepository.findById(PERSISTED_DEVICE_ID1)).thenReturn(Optional.of(persistedDevice));
        deviceService.deleteDevice(PERSISTED_DEVICE_ID1, PERSISTED_USER_NAME1);
        verify(deviceRepository, times(1)).findById(PERSISTED_DEVICE_ID1);
        verify(deviceRepository, times(1)).delete(any(Device.class));
    }

    @Test
    public void deleteDevice_failWrongUser() {
        when(deviceRepository.findById(PERSISTED_DEVICE_ID1)).thenReturn(Optional.of(persistedDevice));
        deviceService.deleteDevice(PERSISTED_DEVICE_ID1, PERSISTED_USER_NAME2);
        verify(deviceRepository, times(1)).findById(PERSISTED_DEVICE_ID1);
        verify(deviceRepository, times(0)).delete(any(Device.class));
    }

    @Test
    public void findNotificationsByUsername_mappingToDtoCorrectly() {
        when(notificationRepository.findByUserUsername(PERSISTED_USER_NAME1)).thenReturn(Collections.singletonList(persistedNotification));

        List<DeviceNotificationDto> notificationDtoList = deviceService.findNotificationsByUsername(PERSISTED_USER_NAME1);

        assertThat(notificationDtoList.size(), is(1));
        DeviceNotificationDto dto = notificationDtoList.get(0);
        assertThat(dto.getId(), is(PERSISTED_DEVICE_ID1));
        assertThat(dto.getPayload(), is(PERSISTED_TOKEN1));
        assertThat(dto.getSendTime(), is(toEpoch(NOTIFICATION_TIME)));
        assertThat(dto.getState(), is(PERSISTED_STATE));
    }

    @Test
    public void updateOrCreateNotification_createCorrectly() {
        when(notificationRepository.save(any(DeviceNotification.class))).thenReturn(persistedNotification);

        DeviceNotificationDto secondNotificationDto = DeviceNotificationDto.builder().id(PERSISTED_DEVICE_ID2)
                .sendTime(toEpoch(NOTIFICATION_TIME)).payload(PERSISTED_TOKEN1).build();
        DeviceNotificationDto result = deviceService.updateOrCreateNotification(secondNotificationDto, PERSISTED_USER_NAME1);

        verify(notificationRepository).save(notificationArgumentCaptor.capture());
        DeviceNotification value = notificationArgumentCaptor.getValue();
        assertThat(value.getId(), is(PERSISTED_DEVICE_ID2));
        assertThat(value.getState(), is(PERSISTED_STATE));
        assertThat(value.getPayload(), is(PERSISTED_TOKEN1));
        assertThat(value.getSendTime(), is(NOTIFICATION_TIME));
        assertThat(value.getUser(), is(PERSISTED_APPERTA_USER_1));

        assertThat(result.getId(), is(PERSISTED_DEVICE_ID1));
        assertThat(result.getState(), is(PERSISTED_STATE));
        assertThat(result.getPayload(), is(PERSISTED_TOKEN1));
        assertThat(result.getSendTime(), is(toEpoch(NOTIFICATION_TIME)));
    }

    @Test
    public void updateOrCreateNotification_updateCorrectly() {
        when(notificationRepository.save(any(DeviceNotification.class))).thenReturn(persistedNotification);

        DeviceNotificationDto secondNotificationDto = DeviceNotificationDto.builder().id(PERSISTED_DEVICE_ID2).state(NotificationState.SCHEDULED)
                .sendTime(toEpoch(NOTIFICATION_TIME)).payload("Anewtoken").build();
        deviceService.updateOrCreateNotification(secondNotificationDto, PERSISTED_USER_NAME1);

        verify(notificationRepository).save(notificationArgumentCaptor.capture());
        DeviceNotification value = notificationArgumentCaptor.getValue();
        assertThat(value.getId(), is(PERSISTED_DEVICE_ID2));
        assertThat(value.getState(), is(PERSISTED_STATE));
        assertThat(value.getPayload(), is("Anewtoken"));
        assertThat(value.getSendTime(), is(NOTIFICATION_TIME));
        assertThat(value.getUser(), is(PERSISTED_APPERTA_USER_1));
    }

    @Test
    public void updateOrCreateNotification_failUpdateWrongUser() {

        when(notificationRepository.existsById(PERSISTED_DEVICE_ID1)).thenReturn(true);
        when(notificationRepository.findById(PERSISTED_DEVICE_ID1)).thenReturn(Optional.of(persistedNotification));
        when(notificationRepository.save(any(DeviceNotification.class))).thenReturn(persistedNotification);

        DeviceNotificationDto result = deviceService.updateOrCreateNotification(firstNotificationDto, PERSISTED_USER_NAME2);

        verify(notificationRepository, times(1)).findById(PERSISTED_DEVICE_ID1);
        verify(notificationRepository, times(0)).save(any(DeviceNotification.class));
    }

    @Test
    public void updateOrCreateNotification_failWrongState() {
        DeviceNotification persistedNotification2 = DeviceNotification.builder().id(PERSISTED_DEVICE_ID2).user(PERSISTED_APPERTA_USER_1)
                .state(NotificationState.SENT).sendTime(NOTIFICATION_TIME).lastAction(NOTIFICATION_TIME).payload(PERSISTED_TOKEN1).build();

        when(notificationRepository.existsById(PERSISTED_DEVICE_ID2)).thenReturn(true);
        when(notificationRepository.findById(PERSISTED_DEVICE_ID2)).thenReturn(Optional.of(persistedNotification2));
        when(notificationRepository.save(any(DeviceNotification.class))).thenReturn(persistedNotification2);

        DeviceNotificationDto secondNotificationDto = DeviceNotificationDto.builder().id(PERSISTED_DEVICE_ID2).state(NotificationState.SCHEDULED)
                .sendTime(toEpoch(NOTIFICATION_TIME)).payload("Anewtoken").build();
        deviceService.updateOrCreateNotification(secondNotificationDto, PERSISTED_USER_NAME1);

        verify(notificationRepository, times(1)).findById(PERSISTED_DEVICE_ID2);
        verify(notificationRepository, times(0)).save(any(DeviceNotification.class));
    }

    @Test
    public void deleteNotification_deleteCorrectly() {
        when(notificationRepository
                .findById(PERSISTED_DEVICE_ID1)).thenReturn(Optional.of(persistedNotification));
        deviceService.deleteNotification(PERSISTED_DEVICE_ID1, PERSISTED_USER_NAME1);
        verify(notificationRepository, times(1)).findById(PERSISTED_DEVICE_ID1);
        verify(notificationRepository, times(1)).delete(persistedNotification);
    }

    @Test
    public void deleteNotification_failWrongUser() {
        when(notificationRepository
                .findById(PERSISTED_DEVICE_ID1)).thenReturn(Optional.of(persistedNotification));
        deviceService.deleteNotification(PERSISTED_DEVICE_ID1, PERSISTED_USER_NAME2);
        verify(notificationRepository, times(1)).findById(PERSISTED_DEVICE_ID1);
        verify(notificationRepository, times(0)).delete(persistedNotification);
    }
}
