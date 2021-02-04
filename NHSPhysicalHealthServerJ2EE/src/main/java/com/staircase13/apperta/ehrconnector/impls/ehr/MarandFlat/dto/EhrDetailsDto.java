package com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.staircase13.apperta.ehrconnector.interfaces.IEhrDetailsDto;
import lombok.*;

/**
 * EhrDetailsDto contains the basic details for an EHR (record) and the status as returned by the server upon
 * query, create, or update.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrDetailsDto implements IEhrDetailsDto {

    /** The status object from openEHR */
    private EhrStatusDto ehrStatus;

    /** The openEHR id */
    private String ehrId;
}
