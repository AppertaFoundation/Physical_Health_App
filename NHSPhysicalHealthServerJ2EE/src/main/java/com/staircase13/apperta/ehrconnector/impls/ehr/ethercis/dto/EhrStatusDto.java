package com.staircase13.apperta.ehrconnector.impls.ehr.ethercis.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrStatusDto {

    private String subjectId;


    private String subjectNamespace;


    private boolean queryable;


    private boolean modifiable;

}
