package com.staircase13.apperta.entities;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class DeviceNotification implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne
    private User user;

    private LocalDateTime sendTime;

    @NotEmpty
    @NotNull
    private String payload;

    @Enumerated(EnumType.STRING)
    @NotNull
    private NotificationState state;

    private LocalDateTime lastAction;
}
