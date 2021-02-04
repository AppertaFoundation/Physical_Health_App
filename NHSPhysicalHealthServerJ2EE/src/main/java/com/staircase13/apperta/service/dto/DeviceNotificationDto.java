package com.staircase13.apperta.service.dto;

import com.staircase13.apperta.entities.NotificationState;
import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceNotificationDto {

    private Long id;

    private Long sendTime;

    @NotEmpty
    private String payload;

    @NotEmpty
    private NotificationState state;

}
