package com.staircase13.apperta.auth.server;

import com.staircase13.apperta.auth.server.OAuthUser;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class PasswordResetToken  implements Serializable {
    @Id
    private String value;

    @NotNull
    private LocalDateTime issued;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Status status;

    @ManyToOne
    @NotNull
    private OAuthUser user;

    public enum Status {
        ISSUED, USED
    }
}
