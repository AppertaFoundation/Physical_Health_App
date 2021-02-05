package com.staircase13.apperta.api;

import com.staircase13.apperta.api.errors.ApiAuthError;
import com.staircase13.apperta.api.errors.ApiValidationErrors;
import com.staircase13.apperta.api.errors.ErrorConstants;
import com.staircase13.apperta.auth.client.AppertaPrinciple;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.entities.Role;
import com.staircase13.apperta.service.EhrRecordService;
import com.staircase13.apperta.service.EhrRecordSession;
import com.staircase13.apperta.service.HcpService;
import com.staircase13.apperta.service.UserService;
import com.staircase13.apperta.service.dto.*;
import com.staircase13.apperta.service.exception.AppertaException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/ehr")
public class HealthRecordEndpoint {

    private final EhrRecordService ehrRecordService;

    private final UserService userService;

    private final HcpService hcpService;

    @Autowired
    public HealthRecordEndpoint(EhrRecordService ehrRecordService, UserService userService, HcpService hcpService){
        this.ehrRecordService = ehrRecordService;
        this.userService = userService;
        this.hcpService = hcpService;
    }

    @ApiOperation(value = "create composition", notes = "Create a new composition on a patient record")
    @ApiResponses({
            @ApiResponse(code = 201, message = "The composition has been created successfully", response = String.class),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('CREATE_EHR')")
    @PostMapping("/composition")
    public void createComposition(@Valid @RequestBody CompositionDto compositionDto,
                                  @AuthenticationPrincipal AppertaPrinciple userDetails)
                                    throws AppertaException {

        IdentifiedParty identifiedParty = getIdentifiedParty(userDetails, compositionDto.getUsername());
        String username = StringUtils.isNotEmpty(compositionDto.getUsername()) ? compositionDto.getUsername() : userDetails.getUsername();

        EhrRecordSession ehrSession = ehrRecordService.findOrCreateEhr(username, identifiedParty);

        if (compositionDto.getParameterMap() != null) {
            ehrRecordService.createTemplatedComposition(ehrSession, compositionDto, identifiedParty);
        } else {
            ehrRecordService.createComposition(ehrSession, compositionDto, identifiedParty);
        }
    }

    @ApiOperation(value = "Fetch composition", notes = "Get a composition version on a patient record")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The composition was found", response = String.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 404, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = String.class)
    })
    @PreAuthorize("hasAuthority('QUERY_EHR')")
    @GetMapping("/composition")
    public CompositionResultDto getComposition(@RequestParam String compositionUid, @RequestParam String username,
                                               @AuthenticationPrincipal AppertaPrinciple userDetails)
            throws AppertaException {
        IdentifiedParty identifiedParty = getIdentifiedParty(userDetails, username);
        String workingUsername = StringUtils.isNotEmpty(username) ? username : userDetails.getUsername();

        EhrRecordSession ehrSession = ehrRecordService.findOrCreateEhr(workingUsername, identifiedParty);
        return ehrRecordService.getCompositionByUid(ehrSession, compositionUid);
    }

    @ApiOperation(value = "Update composition", notes = "Update a composition on a patient record")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The composition has been updated successfully", response = String.class),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('CREATE_EHR')")
    @PutMapping("/composition")
    public void createComposition(@RequestParam String compositionUid, @Valid @RequestBody CompositionDto compositionDto,
                                  @AuthenticationPrincipal AppertaPrinciple userDetails)
            throws AppertaException {

        IdentifiedParty identifiedParty = getIdentifiedParty(userDetails, compositionDto.getUsername());
        String username = StringUtils.isNotEmpty(compositionDto.getUsername()) ? compositionDto.getUsername() : userDetails.getUsername();

        EhrRecordSession ehrSession = ehrRecordService.findOrCreateEhr(username, identifiedParty);

        if (compositionDto.getParameterMap() != null) {
            ehrRecordService.updateTemplatedComposition(ehrSession, compositionUid, compositionDto, identifiedParty);
        } else {
            ehrRecordService.updateComposition(ehrSession, compositionUid, compositionDto, identifiedParty);
        }
    }

    @ApiOperation(value = "Run query", notes = "Run an ehr query on a patient record. The result of the query is shown in the documentation as a string but will always contain a JSON Array.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "The composition has been created successfully", response = String.class),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('QUERY_EHR')")
    @PostMapping("/query")
    public EhrQueryResultDto runBasicQuery(@Valid @RequestBody EhrQueryDto ehrQueryDto,
                                           @AuthenticationPrincipal AppertaPrinciple userDetails)
            throws AppertaException {

        IdentifiedParty identifiedParty = getIdentifiedParty(userDetails, ehrQueryDto.getUsername());
        String username = StringUtils.isNotEmpty(ehrQueryDto.getUsername()) ? ehrQueryDto.getUsername() : userDetails.getUsername();

        EhrRecordSession ehrSession = ehrRecordService.findOrCreateEhr(username, identifiedParty);

        EhrQueryResultDto resultDto = null;
        if (ehrQueryDto.getParameterMap() != null || ehrQueryDto.getQueryTemplate() != null) {
            resultDto = ehrRecordService.executeTemplatedQuery(ehrSession, ehrQueryDto);
        } else {
            resultDto = ehrRecordService.executeDirectQuery(ehrSession, ehrQueryDto);
        }

        return resultDto;
    }

    @ApiOperation(value = "Run multiple queries together", notes = "Run a set of ehr queries on a patient record. The results will be processed and returned together.")
    @ApiResponses({
            @ApiResponse(code = 201, message = "The composition has been created successfully", response = String.class),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('QUERY_EHR')")
    @PostMapping("/multiquery")
    public MultiQueryResultDto runMultipleQuery(@Valid @RequestBody MultiEhrQueryDto multiQueryDto,
                                                @AuthenticationPrincipal AppertaPrinciple userDetails)
        throws AppertaException {

        IdentifiedParty identifiedParty = getIdentifiedParty(userDetails, multiQueryDto.getUsername());
        String username = StringUtils.isNotEmpty(multiQueryDto.getUsername()) ? multiQueryDto.getUsername() : userDetails.getUsername();

        EhrRecordSession ehrSession = ehrRecordService.findOrCreateEhr(username, identifiedParty);

        return ehrRecordService.executeMultipleQueries(ehrSession, multiQueryDto);
    }

    private IdentifiedParty getIdentifiedParty(AppertaPrinciple userDetails, String requestUsername) throws AppertaException {
        String username = userDetails.getUsername();
        IdentifiedParty identifiedParty = null;
        // Check for requested username, and if it matches user
        if (StringUtils.isNotEmpty(requestUsername)) {
            if (userDetails.getRole() == Role.PATIENT && !requestUsername.equalsIgnoreCase(userDetails.getUsername())) {
                throw new AppertaException("apperta.unauthorized.patient");
            } else if (userDetails.getRole() == Role.PATIENT) {
                identifiedParty = ehrRecordService.getPatientIdentifiedParty();
            } else if (userDetails.getRole() == Role.HCP) {
                // Check if HCP associated with this user
                if (!userService.isHCPAssociatedWithPatient(userDetails.getUsername(), requestUsername)) {
                    throw new AppertaException("apperta.unauthorized.hcp");
                }
                HcpDto hcp = hcpService.getProfile(userDetails.getUsername());
                identifiedParty = ehrRecordService.createIdentifiedPartyFromHCP(hcp);
            } else {
                throw new AppertaException("apperta.unauthorized.role", userDetails.getRole().toString());
            }
        } else {
            identifiedParty = ehrRecordService.getPatientIdentifiedParty();
        }
        return identifiedParty;
    }
}
