package com.staircase13.apperta.ehrconnector.impls.ehr.ethercis;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Session extends EhrBaseResponse {

    // The session id
    private String sessionId;

    @Builder
    public Session(String action, Meta meta, String sessionId){
        super(action, meta);
        this.sessionId = sessionId;
    }
}
