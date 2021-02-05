package com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.staircase13.apperta.ehrconnector.KeepAsJsonDeserializer;
import com.staircase13.apperta.ehrconnector.impls.MarandBase.EhrBaseResponse;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarandCompositionResponse extends EhrBaseResponse {

    private String format;

    private String templateId;

    @JsonDeserialize( using = KeepAsJsonDeserializer.class )
    private String composition;

    private boolean deleted;

    private boolean lastVersion;

    @Builder
    public MarandCompositionResponse(String action, Meta meta, String format, String templateId, String composition,
                                     boolean deleted, boolean lastVersion){
        super(action, meta);
        this.format = format;
        this.templateId = templateId;
        this.composition = composition;
        this.deleted = deleted;
        this.lastVersion = lastVersion;
    }
}
