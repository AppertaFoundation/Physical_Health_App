package com.staircase13.apperta.ehrconnector;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CompositionResult {

    private boolean success;

    private String compositionUid;
}
