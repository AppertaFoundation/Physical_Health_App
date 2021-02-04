package com.staircase13.apperta.ehrconnector;

import lombok.*;

/**
 * The identified party responsible for taking actions on the EHR
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IdentifiedParty {


    private String name;

    private String number;

}
