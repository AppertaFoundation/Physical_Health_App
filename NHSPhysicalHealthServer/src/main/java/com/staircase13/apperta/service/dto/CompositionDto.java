package com.staircase13.apperta.service.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(value = "Composition", description = "Represents the data for a composition which should be stored in openEhr.")
public class CompositionDto {

    @NotNull
    @ApiModelProperty(notes = "The date associated with the record", required = true)
    private long recordDate;

    @NotBlank
    @ApiModelProperty(notes = "The template used to store the composition", required = true)
    private String ehrTemplateName;

    @NotNull
    @ApiModelProperty(notes = "Collections of key value pairs representing the content to be stored. Each top level key represents a collection prefix. Each prefix may store an array of objects, and each object contains a map of key value pairs. The prefix key will be added to the key to create the full ehr path.", required = true)
    private Map<String, List<Map<String, String> > > contents;

    @ApiModelProperty(notes = "The primaryEntityName of the parameter map, if one exists, which has previously been registered with the server for the app to map simple content keys to openEhr AQL paths", required = true)
    private String parameterMap;

    @ApiModelProperty(notes = "The user for whom the composition should be stored, or empty if for the current user")
    private String username;
}
