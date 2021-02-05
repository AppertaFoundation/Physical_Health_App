package com.staircase13.apperta.api;

import com.staircase13.apperta.api.errors.ApiAuthError;
import com.staircase13.apperta.api.errors.ApiValidationErrors;
import com.staircase13.apperta.api.errors.CommonApiResponses;
import com.staircase13.apperta.api.errors.ErrorConstants;
import com.staircase13.apperta.auth.client.AppertaPrinciple;
import com.staircase13.apperta.service.HcpService;
import com.staircase13.apperta.service.dto.HcpDto;
import com.staircase13.apperta.service.dto.HcpSummaryDto;
import com.staircase13.apperta.service.exception.InvalidHcpUsernameException;
import com.staircase13.apperta.service.exception.NhsIdAlreadyRegisteredException;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Optional;

@Api(tags = "User", description = "HCP Operations")
@RestController
@RequestMapping("/api/user/hcp")
@CommonApiResponses
public class HcpEndpoint {

    private final HcpService hcpService;

    @Autowired
    public HcpEndpoint(HcpService hcpService) {
        this.hcpService = hcpService;
    }

    @ApiOperation(value = "Search for HCP",notes = "Find an HCP using the corresponding NHS ID. Callers must have the 'Patient' role")
    @ApiResponses({
        @ApiResponse(code = 200, message = "HCP information for the given NHS ID", response = HcpSummaryDto.class),
        @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
        @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class),
        @ApiResponse(code = 404, message = "Indicates no HCP can be found with the given NHS ID", response = HcpSummaryDto.class)
    })
    @PreAuthorize("hasAuthority('HCP_SEARCH')")
    @GetMapping(path = "/search", produces = "application/json")
    public ResponseEntity<HcpSummaryDto> findByNhsId(@RequestParam  @ApiParam(value="NHS ID to search for") String nhsId) {
        Optional<HcpSummaryDto> hcpDtoOptional = hcpService.findByNhsId(nhsId);

        if(hcpDtoOptional.isPresent()) {
            return new ResponseEntity<>(hcpDtoOptional.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ApiOperation(value = "Retrieve HCP Profile",notes = "Return HCP information for the authenticated HCP. Caller must have the 'HCP' role")
    @ApiResponses({
            @ApiResponse(code = 200, message = "HCP information for the authenticated user. If the HCP has not yet added additional information (using the POST method), only the username and email address will be populated.", response = HcpDto.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class),
    })
    @PreAuthorize("hasAuthority('HCP_GET_PROFILE')")
    @GetMapping(path = "/profile", produces = "application/json")
    public HcpDto getProfile(@AuthenticationPrincipal AppertaPrinciple userDetails) throws InvalidHcpUsernameException {
        return hcpService.getProfile(userDetails.getUsername());
    }

    @ApiOperation(value = "Create/Update HCP Profile",notes = "Create or Update HCP information for the authenticated HCP. Caller must have the 'HCP' role")
    @ApiResponses({
            @ApiResponse(code = 200, message = "The HCP has been successfully updated with the given information"),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('HCP_UPDATE_PROFILE')")
    @PostMapping(path = "/profile", produces = "application/json")
    public void createUpdateProfile(@Valid @RequestBody HcpDto hcpDto,  @AuthenticationPrincipal AppertaPrinciple userDetails) throws InvalidHcpUsernameException, IncorrectHcpUsernameException, NhsIdAlreadyRegisteredException {
        String authenticatedUsername = userDetails.getUsername();
        if(!authenticatedUsername.equals(hcpDto.getUsername())) {
            throw new IncorrectHcpUsernameException(hcpDto.getUsername(), authenticatedUsername);
        }
        hcpService.updateProfile(hcpDto);
    }
}
