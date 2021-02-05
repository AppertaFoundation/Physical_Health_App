package com.staircase13.apperta.api.dto;

import com.staircase13.apperta.service.dto.DeviceDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllDevicesResponseDTO {

    private int pageSize;

    private int start;

    private List<DeviceDto> devices;
}
