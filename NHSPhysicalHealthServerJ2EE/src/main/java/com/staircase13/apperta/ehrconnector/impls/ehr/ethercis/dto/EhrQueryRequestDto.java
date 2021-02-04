package com.staircase13.apperta.ehrconnector.impls.ehr.ethercis.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EhrQueryRequestDto {

    private String aql;
}
