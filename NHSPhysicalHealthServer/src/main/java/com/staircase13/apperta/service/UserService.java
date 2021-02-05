package com.staircase13.apperta.service;

import com.staircase13.apperta.auth.server.AuthServerUserDetailsService;
import com.staircase13.apperta.ehrconnector.ConfigConstants;
import com.staircase13.apperta.ehrconnector.HcpDemographicsDto;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.backend.BackendService;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.entities.Role;
import com.staircase13.apperta.entities.User;
import com.staircase13.apperta.service.dto.*;
import com.staircase13.apperta.service.exception.AppertaException;
import com.staircase13.apperta.service.exception.InvalidHcpUsernameException;
import com.staircase13.apperta.service.exception.PatientUsernameNotFoundException;
import com.staircase13.apperta.service.exception.UsernameAlreadyRegisteredException;
import org.apache.commons.lang3.StringUtils;
import com.staircase13.apperta.repository.AppertaUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    private final AppertaUserRepository appertaUserRepository;

    private final BackendService backendService;

    private final EhrDemographicsService demographicsService;

    private final HcpService hcpService;

    private final IdentifiedParty  patientParty;

    private final AuthServerUserDetailsService authServerUserDetailsService;

    @Autowired
    public UserService(AppertaUserRepository appertaUserRepository,
                       BackendService backendService,
                       EhrDemographicsService demographicsService,
                       Environment environment,
                       HcpService hcpService, AuthServerUserDetailsService authServerUserDetailsService) {
        this.appertaUserRepository = appertaUserRepository;
        this.backendService = backendService;
        this.demographicsService = demographicsService;
        this.hcpService = hcpService;
        this.authServerUserDetailsService = authServerUserDetailsService;

        String patientCommitterName = environment.getProperty(ConfigConstants.PATIENT_COMMITTER_NAME);
        String patientCommitterNumber = environment.getProperty(ConfigConstants.PATIENT_COMMITTER_NUMBER);
        patientParty = IdentifiedParty.builder()
                .name(patientCommitterName)
                .number(patientCommitterNumber)
                .build();
    }

    public UserDto createUser(UserDto user) throws UsernameAlreadyRegisteredException {

        final String username = user.getUsername();

        LOGGER.debug("Creating user '{}'",username);

        if(appertaUserRepository.existsByUsername(username)) {
            throw new UsernameAlreadyRegisteredException(username);
        }

        User generatedUser = appertaUserRepository.save(mapDtoToUser(user));
        LOGGER.debug("User created with ID '{}'", generatedUser.getId());

        authServerUserDetailsService.registerUser(
                user.getUsername(),
                user.getPassword(),
                user.getEmailAddress());

        return mapUserToDto(generatedUser);
    }

    public ProfileDto fetchUserDemographics(String username) throws EhrGenericClientException, EhrAuthenticationException, EhrOperationException {
        Optional<ProfileDto> profileOptional = demographicsService.findProfileByUsername(username);

        if (profileOptional.isPresent()) {
            return profileOptional.get();
        }
        return new ProfileDto();
    }

    public ProfileDto updateUserDemographics(ProfileDto profileDto, String username, @Nullable IdentifiedParty identifiedParty)
            throws AppertaException {

        ProfileDto profile = null;
        Optional<User> userOptional = appertaUserRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            if (identifiedParty == null) {
                identifiedParty = patientParty;
            }

            Optional<ProfileDto> profileOptional = demographicsService.updateOrCreateEhr(profileDto, username, identifiedParty);
            if (profileOptional.isPresent()) {
                profile = profileOptional.get();
            }
        } else {
            throw new IllegalStateException("User demographics update called on missing user record");
        }
        return profile;
    }

    /**
     *
     * Precondition: user is a patient
     * @param username The username for which to fetch hcp details
     * @return
     */
    public PatientHcpDto fetchPatientHCPs(String username) throws AppertaException {

        PatientHcpDto patientHcpDto = new PatientHcpDto();
        Optional<HcpDemographicsDto> optionalHcps = demographicsService.getNHSIdsForPatientHCPs(username);
        if (optionalHcps.isPresent()) {
            HcpDemographicsDto hcpDemographicsDto = optionalHcps.get();
            patientHcpDto.setCareProfessionals(hcpService.populateDtoList(hcpDemographicsDto.getHCPIds()));
            // primary Id should always be in the full list
            String primaryId = hcpDemographicsDto.getPrimaryHCPId();
            if (primaryId != null) {
                for (HcpSummaryDto summaryDto : patientHcpDto.getCareProfessionals()) {
                    if (summaryDto.getNhsId().equals(primaryId)) {
                        patientHcpDto.setPrimaryCareProfessional(summaryDto);
                        break;
                    }
                }
            }
        }
        return patientHcpDto;
    }

    public PatientHcpDto updatePatientHCPs(PatientHcpDto patientHcpDto, String username, @Nullable IdentifiedParty identifiedParty)
            throws AppertaException {

        Optional<User> userOptional = appertaUserRepository.findByUsername(username);

        if (userOptional.isPresent()) {
            if (identifiedParty == null) {
                identifiedParty = patientParty;
            }
            demographicsService.updatePatientHCPs(patientHcpDto, username, identifiedParty);
        } else {
            throw new IllegalStateException("User demographics update called on missing user record");
        }

        return fetchPatientHCPs(username);
    }

    /**
     * This method checks if the patient record contains the
     * @param username
     * @param patientUsername
     * @return
     */
    public boolean isHCPAssociatedWithPatient(String username, String patientUsername)
            throws InvalidHcpUsernameException, EhrAuthenticationException, EhrOperationException, PatientUsernameNotFoundException {

        HcpDto profDto = hcpService.getProfile(username);
        Optional<HcpDemographicsDto> optionalHcps = demographicsService.getNHSIdsForPatientHCPs(patientUsername);
        if (optionalHcps.isPresent() && StringUtils.isNotEmpty(profDto.getNhsId())) {
            List<String> hcps = optionalHcps.get().getHCPIds();
            return hcps.contains(profDto.getNhsId());
        }
        return false;
    }


    public Map<String,String> getBackendInfo(String username) {
        return backendService.getUserInfo(username);
    }

    private UserDto mapUserToDto(User source) {
        UserDto target = new UserDto();
        BeanUtils.copyProperties(source,target);
        return target;
    }

    private User mapDtoToUser(UserDto source) {
        User target = new User();
        BeanUtils.copyProperties(source, target);
        return target;
    }
}
