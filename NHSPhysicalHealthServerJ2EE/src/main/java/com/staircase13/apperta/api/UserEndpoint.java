package com.staircase13.apperta.api;

import com.staircase13.apperta.api.errors.*;
import com.staircase13.apperta.auth.client.AppertaPrinciple;
import com.staircase13.apperta.auth.server.PasswordService;
import com.staircase13.apperta.service.EhrRecordService;
import com.staircase13.apperta.service.EhrRecordSession;
import com.staircase13.apperta.service.UserService;
import com.staircase13.apperta.service.dto.PatientHcpDto;
import com.staircase13.apperta.service.dto.PermissionsDto;
import com.staircase13.apperta.service.dto.ProfileDto;
import com.staircase13.apperta.service.dto.UserDto;
import com.staircase13.apperta.service.exception.AppertaException;
import com.staircase13.apperta.service.exception.InvalidTokenException;
import com.staircase13.apperta.service.exception.UsernameAlreadyRegisteredException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "User", description = "User Operations")
@RestController
@RequestMapping("/api/user")
@CommonApiResponses
public class UserEndpoint {

    private final PasswordService passwordService;

    private final UserService userService;

    private final EhrRecordService ehrRecordService;

    @Autowired
    public UserEndpoint(PasswordService passwordService, UserService userService, EhrRecordService ehrRecordService) {
        this.userService = userService;
        this.passwordService = passwordService;
        this.ehrRecordService = ehrRecordService;
    }

    @ApiOperation(value = "passwordResetTokenRequest",notes = "Used to request a password reset token. If the username doesn't exist, this method will still return a HTTP 200. The token will be emailed to the user's email address. Requires a Client Credentials Token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "A Password Reset Token has been emailed to the user, if the user exists"),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('ROLE_TRUSTED_CLIENT')")
    @PostMapping("/passwordResetTokenRequest")
    public void passwordResetTokenRequest(@Valid @RequestBody PasswordResetTokenRequest requestTokenRequestDto) {
        passwordService.issuePasswordResetToken(requestTokenRequestDto.getUsername());
    }

    @ApiOperation(value = "register", notes = "Used to register new users in the system. Requires a Client Credentials Token")
    @ApiResponses({
            @ApiResponse(code = 201, message = "The user has been successfully registered. Will return a unique user identifier.", response = String.class),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('ROLE_TRUSTED_CLIENT')")
    @PostMapping("/register")
    public UserDto register(@Valid @RequestBody UserDto user) throws UsernameAlreadyRegisteredException {
        return userService.createUser(user);
    }

    @ApiOperation(value = "passwordResetTokenVerify",notes = "Used to verify a reset token is invalid (i.e. it exists, hasn't been used and hasn't expired. Requires a Client Credentials Token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "If the password reset token is valid"),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('ROLE_TRUSTED_CLIENT')")
    @PostMapping("/passwordResetTokenVerify")
    public boolean passwordResetTokenVerify(@Valid @RequestBody PasswordResetTokenVerifyRequest passwordResetTokenVerifyRequest) {
        return passwordService.isValidToken(passwordResetTokenVerifyRequest.getToken());
    }

    @ApiOperation(value = "passwordResetWithToken",notes = "Used to reset a password using a retrieved reset token. Requires a Client Credentials Token")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password has been reset"),
            @ApiResponse(code = 400, message = "The Token is invalid or has expired", response = ApiError.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('ROLE_TRUSTED_CLIENT')")
    @PostMapping("/passwordResetWithToken")
    public void passwordResetWithToken(@Valid @RequestBody PasswordResetWithTokenRequest passwordResetRequest) throws InvalidTokenException {
        passwordService.resetPasswordWithToken(passwordResetRequest.getToken(), passwordResetRequest.getPassword());
    }

    @ApiOperation(value = "passwordResetNoToke",notes = "Used to reset the current user's password")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Password has been reset"),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('RESET_PASSWORD')")
    @PostMapping("/passwordReset")
    public void passwordReset(@Valid @RequestBody PasswordResetRequest passwordResetRequest, @AuthenticationPrincipal AppertaPrinciple userDetails) throws InvalidTokenException {
        passwordService.resetPassword(userDetails.getUsername(), passwordResetRequest.getPassword());
    }

    @ApiOperation(value = "Consents",notes = "Fetches current settings for T&C and permissions for user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "A list of all current permissions objects which are granted or revoked for the patient"),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('VIEW_USER_DETAILS')")
    @GetMapping(path = "/profile/permission", produces = "application/json")
    public PermissionsDto getPermission(@AuthenticationPrincipal AppertaPrinciple userDetails) throws AppertaException {

        EhrRecordSession session = ehrRecordService.findOrCreateEhr(userDetails.getUsername(), null);
        return ehrRecordService.getConsents(session);
    }

    @ApiOperation(value = "Consents",notes = "Updates settings for T&C and permissions for user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "A list of all updated permissions objects which are granted or revoked for the patient"),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('PATIENT_UPDATE_PROFILE')")
    @PostMapping(path = "/profile/permission",  produces = "application/json")
    public PermissionsDto setPermission(@Valid @RequestBody PermissionsDto permissionsDTO, @AuthenticationPrincipal AppertaPrinciple userDetails) throws AppertaException {
        EhrRecordSession session = ehrRecordService.findOrCreateEhr(userDetails.getUsername(), null);
        return ehrRecordService.updateConsents(permissionsDTO, session);
    }

    @ApiOperation(value = "profile",notes = "Fetches current profile details for user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Profile object for a user."),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('VIEW_USER_DETAILS')")
    @GetMapping(path = "/profile", produces = "application/json")
    public ProfileDto getProfile(@AuthenticationPrincipal AppertaPrinciple userDetails) throws AppertaException {
        return userService.fetchUserDemographics(userDetails.getUsername());
    }

    @ApiOperation(value = "profile",notes = "Updates current profile for user")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated profile details for user"),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('VIEW_USER_DETAILS')")
    @PostMapping(path = "/profile",  produces = "application/json")
    public ProfileDto setProfile(@Valid @RequestBody ProfileDto profileDto, @AuthenticationPrincipal AppertaPrinciple userDetails) throws AppertaException {
        return userService.updateUserDemographics(profileDto, userDetails.getUsername(), null);
    }

    @ApiOperation(value = "associated hcps", notes = "Fetches details of healthcare professionals associated with a patient")
    @ApiResponses({
            @ApiResponse(code = 200, message = "A primary healthcare professional and list of associated professionals for a user."),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('VIEW_USER_DETAILS')")
    @GetMapping(path = "/profile/hcp")
    public PatientHcpDto getHealthcareProfessionals(@AuthenticationPrincipal AppertaPrinciple userDetails)
                        throws AppertaException {
        return userService.fetchPatientHCPs(userDetails.getUsername());
    }

    @ApiOperation(value = "associated hcps",notes = "Updates details of healthcare professionals associated with a patient")
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated hcp details for user"),
            @ApiResponse(code = 400, message = ErrorConstants.HTTP_400_API_DESCRIPTION, response = ApiValidationErrors.class),
            @ApiResponse(code = 401, message = ErrorConstants.HTTP_401_API_DESCRIPTION, response = ApiAuthError.class),
            @ApiResponse(code = 403, message = ErrorConstants.HTTP_403_API_DESCRIPTION, response = ApiAuthError.class)
    })
    @PreAuthorize("hasAuthority('PATIENT_UPDATE_PROFILE')")
    @PostMapping(path = "/profile/hcp",  produces = "application/json")
    public PatientHcpDto setHealthcareProfessionals(@Valid @RequestBody PatientHcpDto patientHcpDto,
                                                    @AuthenticationPrincipal AppertaPrinciple userDetails)
                            throws AppertaException {
        return userService.updatePatientHCPs(patientHcpDto, userDetails.getUsername(), null);
    }
}
