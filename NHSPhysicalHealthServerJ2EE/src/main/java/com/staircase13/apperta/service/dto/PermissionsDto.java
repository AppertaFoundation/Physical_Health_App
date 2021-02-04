package com.staircase13.apperta.service.dto;

import com.staircase13.apperta.entities.PermissionState;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionsDto {

    List<Permission> grantedPermissions;

    List<Permission> removedPermissions;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Permission {

        private String name;

        private String description;

        private PermissionState state;
    }
}
