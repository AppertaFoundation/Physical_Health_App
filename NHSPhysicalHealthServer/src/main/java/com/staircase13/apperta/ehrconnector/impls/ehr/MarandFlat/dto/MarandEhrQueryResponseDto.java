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
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MarandEhrQueryResponseDto {

    private EhrBaseResponse.Meta meta;

    private String aql;

    private String executedAql;

    @JsonDeserialize( using = KeepAsJsonDeserializer.class )
    private String resultSet;

}
