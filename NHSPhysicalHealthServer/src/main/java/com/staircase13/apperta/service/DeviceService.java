package com.staircase13.apperta.service;

import com.staircase13.apperta.entities.Device;
import com.staircase13.apperta.entities.DeviceNotification;

import com.staircase13.apperta.entities.NotificationState;
import com.staircase13.apperta.repository.DeviceNotificationRepository;
import com.staircase13.apperta.repository.DeviceRepository;
import com.staircase13.apperta.service.dto.DeviceDto;
import com.staircase13.apperta.service.dto.DeviceNotificationDto;
import com.staircase13.apperta.repository.AppertaUserRepository;
import com.staircase13.apperta.service.exception.AppertaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.staircase13.apperta.service.util.DateTimeUtil.toEpoch;
import static com.staircase13.apperta.service.util.DateTimeUtil.toLocalDateTime;

@Service
public class DeviceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceService.class);

    private final DeviceRepository deviceRepository;

    private final DeviceNotificationRepository deviceNotificationRepository;

    private final AppertaUserRepository appertaUserRepository;

    @Autowired
    public DeviceService(DeviceRepository deviceRepository, DeviceNotificationRepository deviceNotificationRepository, AppertaUserRepository appertaUserRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceNotificationRepository = deviceNotificationRepository;
        this.appertaUserRepository = appertaUserRepository;
    }

    /**
     * Find device by device generated uuid
     * @param uuid The device uuid
     * @return An optional containing the device dto if it exists
     */
    public Optional<DeviceDto> findDeviceByUuid(String uuid) {
        Optional<DeviceDto> result = Optional.empty();
        Optional<Device> deviceOpt = deviceRepository.findByUuid(uuid);
        if (deviceOpt.isPresent()){
            result =  Optional.of(mapDeviceToDto(deviceOpt.get()));
        }
        return result;
    }

    /**
     * Update or create a device for a username
     * @param deviceDTO The device dto from the api
     * @param username The username of the device owner
     * @return The device dto upon success.
     */
    public DeviceDto updateOrCreateDevice(DeviceDto deviceDTO, String username) throws AppertaException {
        if (deviceDTO.getId() > 0) {
            Optional<Device> existingDevice = deviceRepository.findById(deviceDTO.getId());
            if (existingDevice.isPresent() && !existingDevice.get().getUsername().equals(username)){
                throw new AppertaException("apperta.device.notification.wrong_device_user");
            }
        }
        LOGGER.debug("Creating / updating device {} for {}", deviceDTO.getUuid(), username);
        Device device = deviceRepository.save(mapDtoToDevice(deviceDTO, username));
        return mapDeviceToDto(device);
    }

    /**
     * Find all devices belonging to a user
     * @param username The username of the user to search for
     * @return The list of devices associated with a user.
     */
    public List<DeviceDto> findDevicesForUsername(String username){

        return deviceRepository.findByUsername(username).stream().map(d -> mapDeviceToDto(d)).collect(Collectors.toList());
    }

    public List<DeviceDto> findAllDevices(String queryPartialUsername, int pageSize, int pageStart){
        Pageable paging = PageRequest.of(pageStart, pageSize);
        return deviceRepository.findByUsernameStartingWith(queryPartialUsername, paging).stream().map(d -> mapDeviceToDto(d)).collect(Collectors.toList());
    }

    /**
     * Delete a device
     * @param deviceId The device id for the device to delete
     * @param username The username of the device owner
     */
    public void deleteDevice(Long deviceId, String username){
        Optional<Device> device = deviceRepository.findById(deviceId);
        if (device.isPresent() && device.get().getUsername().equals(username)) {
            deviceRepository.delete(device.get());
        }
    }

    /**
     * Find a list of notifications for a user
     * @param username The username to search
     * @return A list of notifications by dto for the user, may be empty. Dto is suitable for apis, but not for internal
     * processing as it does not contain all the fields for sending messages.
     */
    public List<DeviceNotificationDto> findNotificationsByUsername(String username){
        return deviceNotificationRepository.findByUserUsername(username).stream().map(n -> mapNotificationToDto(n)).collect(Collectors.toList());
    }

    /**
     * Try to update or create a notification. This will fail if the notification already exists and is already sent
     * or in the process of being sent.
     * @param notificationDTO The notification dto from the API
     * @param username The username
     * @return The updated notification dto on success, or null if it cannot be updated.
     */
    public DeviceNotificationDto updateOrCreateNotification(DeviceNotificationDto notificationDTO, String username)
        throws AppertaException {

        // Do not update if already complete
        if (notificationDTO.getId() > 0 && deviceNotificationRepository.existsById(notificationDTO.getId())) {
            Optional<DeviceNotification> original = deviceNotificationRepository.findById(notificationDTO.getId());
            if (!original.get().getUser().getUsername().equals(username)){
                // New notification being generated for a different user than the original
                throw new AppertaException("apperta.device.notification.wrong_user");
            }
            NotificationState state = original.get().getState();
            if (state != NotificationState.SCHEDULED) {
                throw new AppertaException("apperta.device.notification.already_complete");
            }
        }

        if (notificationDTO.getState() == null){
            // set initial state on create
            notificationDTO.setState(NotificationState.SCHEDULED);
        }

        DeviceNotification notification = deviceNotificationRepository.save(mapDtoToNotification(notificationDTO, username));
        return mapNotificationToDto(notification);
    }

    /**
     * Delete a notification by id, regardless of current state, providing it belongs to the user.
     * @param notificationId The notification id
     * @param username The username owning the notification
     */
    public void deleteNotification(Long notificationId, String username) {
        Optional<DeviceNotification> notification = deviceNotificationRepository.findById(notificationId);
        if (notification.isPresent() && notification.get().getUser().getUsername().equals(username)) {
           deviceNotificationRepository.delete(notification.get());
       }
    }

    private Device mapDtoToDevice(DeviceDto deviceDto, String username){
        return Device.builder()
                .id(deviceDto.getId())
                .uuid(deviceDto.getUuid())
                .username(username)
                .token(deviceDto.getToken())
                .deviceType(deviceDto.getType())
                .build();
    }

    private DeviceDto mapDeviceToDto(Device device) {
        return DeviceDto.builder()
                .id(device.getId())
                .uuid(device.getUuid())
                .token(device.getToken())
                .type(device.getDeviceType())
                .build();
    }

    private DeviceNotification mapDtoToNotification(DeviceNotificationDto notificationDTO, String username) {
        return DeviceNotification.builder()
                .id(notificationDTO.getId())
                .user(appertaUserRepository.findByUsername(username).get())
                .sendTime(toLocalDateTime(notificationDTO.getSendTime()))
                .payload(notificationDTO.getPayload())
                .state(notificationDTO.getState())
                .build();
    }

    private DeviceNotificationDto mapNotificationToDto(DeviceNotification notification) {
        return DeviceNotificationDto.builder()
                .id(notification.getId())
                .sendTime(toEpoch(notification.getSendTime()))
                .payload(notification.getPayload())
                .sendTime(toEpoch(notification.getSendTime()))
                .state(notification.getState())
                .build();
    }

}
