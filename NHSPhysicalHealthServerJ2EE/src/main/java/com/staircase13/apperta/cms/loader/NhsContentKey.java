package com.staircase13.apperta.cms.loader;

import com.staircase13.apperta.cms.entities.NhsApi;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class NhsContentKey {
    private NhsApi nhsApi;
    private String primaryEntityName;
    private String secondaryEntityName;
}
