package com.staircase13.apperta.ehrconnector.impls.ehr.ethercis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.staircase13.apperta.ehrconnector.impls.ehr.ethercis.EhrBaseResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrCompositionCreateResponse extends EhrBaseResponse {

    // The session id
    private String compositionUid;

    @Builder
    public EhrCompositionCreateResponse(String action, Meta meta, String compositionUid){
        super(action, meta);
        this.compositionUid = compositionUid;
    }
}
