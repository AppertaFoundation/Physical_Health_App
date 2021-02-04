package com.staircase13.apperta.api;


import com.staircase13.apperta.api.dto.PatientSearchRequest;
import com.staircase13.apperta.api.dto.PatientsResponse;
import com.staircase13.apperta.api.errors.ApiAuthError;
import com.staircase13.apperta.api.errors.CommonApiResponses;
import com.staircase13.apperta.api.errors.ErrorConstants;
import com.staircase13.apperta.auth.client.AppertaPrinciple;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.service.EhrDemographicsService;
import com.staircase13.apperta.service.HcpService;
import com.staircase13.apperta.service.dto.HcpDto;
import com.staircase13.apperta.service.dto.ProfileDto;
import com.staircase13.apperta.service.exception.InvalidHcpUsernameException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * Patients endpoint provides services to an admin interface for patient data associated with a admin or HCP.
 */
@Api(description = "HCP / Admin Operations")
@RestController
@CommonApiResponses
@RequestMapping("/api/patients")
public class PatientsEndpoint {

    private final EhrDemographicsService demographicsService;

    private final HcpService hcpService;

    @Autowired
    public PatientsEndpoint(EhrDemographicsService demographicsService, HcpService hcpService) {
        this.demographicsService = demographicsService;
        this.hcpService = hcpService;
    }

    @ApiOperation(value = "patients",notes = "Return list of patients for a healthcare professional")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of Patients and paging details"),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @GetMapping(produces = "application/json")
    @PreAuthorize("hasAuthority('LIST_PATIENTS')")
    public PatientsResponse findPatients(@RequestParam int start, @RequestParam int pageSize,
                                         @AuthenticationPrincipal AppertaPrinciple userDetails)
                                            throws InvalidHcpUsernameException, EhrAuthenticationException {
// TODO: handle paging
        HcpDto hcpDto = hcpService.getProfile(userDetails.getUsername());
        List<ProfileDto> profiles;
        if (hcpDto.getNhsId() != null) {
            profiles = demographicsService.findProfilesByHCPId(hcpDto.getNhsId());
        } else {
            profiles = Collections.emptyList();
        }
        return PatientsResponse.builder()
                .start(start)
                .pageSize(pageSize)
                .profiles(profiles)
                .build();
    }

    @ApiOperation(value = "patients",notes = "Return list of patients for a healthcare professional with search terms")
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of Patients plus search term and paging details"),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PostMapping(produces = "application/json")
    @PreAuthorize("hasAuthority('LIST_PATIENTS')")
    public PatientsResponse findPatients(@Valid @RequestBody PatientSearchRequest patientSearchRequest,
                                         @AuthenticationPrincipal AppertaPrinciple userDetails)
                                            throws InvalidHcpUsernameException, EhrAuthenticationException {
// TODO: handle paging and search term
        HcpDto hcpDto = hcpService.getProfile(userDetails.getUsername());
        List<ProfileDto> profiles;
        if (hcpDto.getNhsId() != null) {
            profiles = demographicsService.findProfilesByHCPId(hcpDto.getNhsId());
        } else {
            profiles = Collections.emptyList();
        }
        return PatientsResponse.builder()
                .start(patientSearchRequest.getStart())
                .pageSize(patientSearchRequest.getPageSize())
                .searchTerm(patientSearchRequest.getSearchTerm())
                .profiles(profiles)
                .build();
    }

}
