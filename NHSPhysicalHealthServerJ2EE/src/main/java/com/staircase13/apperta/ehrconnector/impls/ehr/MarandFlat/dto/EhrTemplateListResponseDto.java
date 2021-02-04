package com.staircase13.apperta.ehrconnector.impls.ehr.MarandFlat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EhrTemplateListResponseDto {

    private List<TemplateItem> templates;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TemplateItem {

        private String templateId;

        private LocalDateTime createdOn;
    }
}
