package com.staircase13.apperta.ehrconnector;


import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HcpDemographicsDto {

    private String primaryHCPId;

    private List<String> HCPIds;
}
