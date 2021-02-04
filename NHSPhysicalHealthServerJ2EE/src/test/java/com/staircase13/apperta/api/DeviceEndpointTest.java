package com.staircase13.apperta.api;

import com.staircase13.apperta.auth.client.AppertaPrinciple;
import com.staircase13.apperta.service.DeviceService;
import com.staircase13.apperta.service.dto.DeviceDto;
import com.staircase13.apperta.service.dto.DeviceNotificationDto;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

import static org.mockito.Mockito.verify;

public class DeviceEndpointTest {

    private static final String USERNAME = "Username1";
    private static final String AUTHORITY = "AppertaAuthority";

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @InjectMocks
    private DeviceEndpoint deviceEndpoint;

    @Mock
    private DeviceService deviceService;

    private AppertaPrinciple userDetails;

    @Before
    public void setup() {
        Collection<GrantedAuthority> authorities = Collections.singletonList((GrantedAuthority) () -> AUTHORITY);
        userDetails = AppertaPrinciple.builder()
                .username(USERNAME)
                .password("password")
                .authorities(authorities)
                .enabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .build();
    }

    @Test
    public void register(){
        DeviceDto deviceDto = DeviceDto.builder().build();
        deviceEndpoint.register(deviceDto, userDetails);
        verify(deviceService).updateOrCreateDevice(deviceDto, USERNAME);
    }

    @Test
    public void unregister(){
        deviceEndpoint.unregister(123L, userDetails);
        verify(deviceService).deleteDevice(123L, USERNAME);
    }

    @Test
    public void getDevices(){
        deviceEndpoint.getDevices(userDetails);
        verify(deviceService).findDevicesForUsername(USERNAME);
    }

    @Test
    public void getReminders(){
        deviceEndpoint.getReminders(userDetails);
        verify(deviceService).findNotificationsByUsername(USERNAME);
    }

    @Test
    public void updateReminder(){
        DeviceNotificationDto dto = DeviceNotificationDto.builder().build();
        deviceEndpoint.updateReminder(dto, userDetails);
        verify(deviceService).updateOrCreateNotification(dto, USERNAME);
    }

    @Test
    public void deleteReminder(){
        deviceEndpoint.deleteReminder(123L, userDetails);
        verify(deviceService).deleteNotification(123L, USERNAME);
    }

}
