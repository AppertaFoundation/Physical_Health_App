package com.staircase13.apperta.api;

import com.staircase13.apperta.api.dto.AllDevicesResponseDTO;
import com.staircase13.apperta.auth.client.AppertaPrinciple;
import com.staircase13.apperta.service.DeviceService;
import com.staircase13.apperta.service.dto.DeviceDto;
import com.staircase13.apperta.service.dto.DeviceNotificationDto;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * This class is responsible for tracking devices and requested notifications in order to provide push notifications
 * back to the appropriate devices for the user.
 */
@Api(tags = "Device")
@RestController
@RequestMapping("/api/device")
public class DeviceEndpoint {

    private final DeviceService deviceService;

    @Autowired
    public DeviceEndpoint(DeviceService deviceService) {
        this.deviceService = deviceService;
    }

    @PostMapping("/register")
    @PreAuthorize("hasAuthority('GET_SET_DEVICE')")
    public DeviceDto register(@Valid @RequestBody DeviceDto device, @AuthenticationPrincipal AppertaPrinciple userDetails) {
        return deviceService.updateOrCreateDevice(device, userDetails.getUsername());
    }

    @DeleteMapping("/unregister")
    @PreAuthorize("hasAuthority('GET_SET_DEVICE')")
    public void unregister(@RequestParam long deviceId, @AuthenticationPrincipal AppertaPrinciple userDetails) {
        deviceService.deleteDevice(deviceId, userDetails.getUsername());
    }

    @GetMapping("/query")
    @PreAuthorize("hasAuthority('ALL_DEVICES')")
    public AllDevicesResponseDTO getDevices(@AuthenticationPrincipal AppertaPrinciple userDetails) {
        List<DeviceDto> devices = deviceService.findDevicesForUsername(userDetails.getUsername());
        AllDevicesResponseDTO dto = AllDevicesResponseDTO.builder()
                .pageSize(50)
                .start(0)
                .devices(devices)
                .build();
        return dto;
    }

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ALL_DEVICES')")
    public AllDevicesResponseDTO getAll(@RequestParam String partialUsername, @RequestParam int pageSize,
                                        @RequestParam int pageStart, @AuthenticationPrincipal AppertaPrinciple userDetails) {
        // TODO: handle pagination properly
        List<DeviceDto> devices =  deviceService.findAllDevices(partialUsername, pageSize, pageStart);
        return AllDevicesResponseDTO.builder()
                .devices(devices)
                .pageSize(pageSize)
                .start(pageStart)
                .build();
    }

    @GetMapping("/remind")
    @PreAuthorize("hasAuthority('GET_SET_DEVICE')")
    public List<DeviceNotificationDto> getReminders(@AuthenticationPrincipal AppertaPrinciple userDetails) {
        return deviceService.findNotificationsByUsername(userDetails.getUsername());
    }

    @PostMapping("/remind")
    @PreAuthorize("hasAuthority('GET_SET_DEVICE')")
    public DeviceNotificationDto updateReminder(@Valid @RequestBody DeviceNotificationDto notificationDTO,
                                                @AuthenticationPrincipal AppertaPrinciple userDetails) {
        // TODO: how is the payload being build for reminder notifications?
        notificationDTO = deviceService.updateOrCreateNotification(notificationDTO, userDetails.getUsername());
        // TODO: if notificationDTO is null throw
        return notificationDTO;
    }

    @DeleteMapping("/remind")
    @PreAuthorize("hasAuthority('GET_SET_DEVICE')")
    public void deleteReminder(@RequestParam long notificationId, @AuthenticationPrincipal AppertaPrinciple userDetails) {
        deviceService.deleteNotification(notificationId, userDetails.getUsername());
    }

}
