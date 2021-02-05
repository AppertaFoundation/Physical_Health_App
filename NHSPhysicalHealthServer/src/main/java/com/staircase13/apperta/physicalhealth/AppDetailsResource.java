package com.staircase13.apperta.physicalhealth;


import com.staircase13.apperta.service.dto.CompositionParametersDto;
import com.staircase13.apperta.service.dto.QueryTemplateDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppDetailsResource {

    private String appName;

    private List<String> templates;

    private List<CompositionParametersDto> params;

    private List<QueryTemplateDto> queries;
}
