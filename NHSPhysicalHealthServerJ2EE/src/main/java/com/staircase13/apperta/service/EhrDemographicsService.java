package com.staircase13.apperta.service;

import com.staircase13.apperta.ehrconnector.HcpDemographicsDto;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.exception.EhrServerException;
import com.staircase13.apperta.ehrconnector.interfaces.IDemographicsDto;
import com.staircase13.apperta.ehrconnector.interfaces.IDemographicsRecord;
import com.staircase13.apperta.service.dto.PatientHcpDto;
import com.staircase13.apperta.service.dto.ProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EhrDemographicsService {

    private static final Logger LOG = LoggerFactory.getLogger(EhrDemographicsService.class);

    // TODO:  Sort out all exception handling
    // TODO: sort out caching

    /** The connection to the underlying EHR */
    private final IDemographicsRecord demographicsRecordProvider;

    @Autowired
    public EhrDemographicsService(IDemographicsRecord demographicsRecord) {
        this.demographicsRecordProvider = demographicsRecord;
    }

    /**
     * Find profile, if it exists, which matches username
     * @param username The username to search
     * @return An Optional containing the profile
     */
    @Cacheable("ehrDemographicsProfile")
    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public Optional<ProfileDto> findProfileByUsername(String username) throws EhrAuthenticationException {
        LOG.debug("Find profile by username [{}]",username);

        Optional<ProfileDto> profileResult = Optional.empty();
        try {
           IDemographicsDto demographicsDto = demographicsRecordProvider.findByLocalUsername(username);

            if (demographicsDto != null) {
                profileResult = Optional.of(demographicsRecordProvider.mapEhrToProfile(demographicsDto));
            }
        } catch (EhrOperationException oe) {
            LOG.warn("Operation exception", oe);
            // TODO
        }
        return profileResult;
    }

    /**
     * Find profiles which are associated with the HCP
     * @param careProfessionalId
     * @return
     */
    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public List<ProfileDto> findProfilesByHCPId(String careProfessionalId) throws EhrAuthenticationException {
        List<ProfileDto> profiles = new ArrayList<>();

        try {
            List<IDemographicsDto> hcpDemographicsList = demographicsRecordProvider.findByHCP(careProfessionalId);

            for(IDemographicsDto demographicsDto : hcpDemographicsList){
                profiles.add(demographicsRecordProvider.mapEhrToProfile(demographicsDto));
            }

        } catch (EhrOperationException oe) {
            LOG.warn("Operation exception", oe);
            // TODO
        }

        return profiles;
    }

    /**
     * Update or create a health record demographics entry for a username.
     * @param profileDto The profile for the user
     * @param username The username to create or update
     * @param identifiedParty The committing party
     * @return
     */
    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public Optional<ProfileDto> updateOrCreateEhr(ProfileDto profileDto, String username, IdentifiedParty identifiedParty)
            throws EhrAuthenticationException {

        Optional<ProfileDto> profileResult = Optional.empty();
        try {
            IDemographicsDto updatedDemographicsDto = demographicsRecordProvider.mapProfileToEhr(profileDto, null, username);
            IDemographicsDto demographicsDto = demographicsRecordProvider.findByLocalUsername(username);
            if (demographicsDto != null) {
                HcpDemographicsDto hcps = demographicsRecordProvider.getHCPsFromDto(demographicsDto);

                // Update record
                updatedDemographicsDto.setPartyId(demographicsDto.getPartyId());
                if (demographicsRecordProvider.update(updatedDemographicsDto, hcps, identifiedParty)){
                    demographicsDto = updatedDemographicsDto;
                }
            } else {
                // Create record
                long partyId = demographicsRecordProvider.create(updatedDemographicsDto, identifiedParty);
                if (partyId > 0){
                    demographicsDto = demographicsRecordProvider.findByPartyId(partyId);
                }
            }

            if (demographicsDto != null) {
                profileResult = Optional.of(demographicsRecordProvider.mapEhrToProfile(demographicsDto));
            }

        } catch (EhrOperationException oe) {
            LOG.warn("Operation exception", oe);
            // TODO
        }
        return profileResult;
    }

    /**
     *
     * @param username
     * @return
     */
    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public Optional<HcpDemographicsDto> getNHSIdsForPatientHCPs(String username) throws EhrAuthenticationException {

       Optional<HcpDemographicsDto> optionalHcps = Optional.empty();
        try {
            IDemographicsDto demographicsDto = demographicsRecordProvider.findByLocalUsername(username);

            if (demographicsDto != null) {
                optionalHcps =  Optional.of(demographicsRecordProvider.getHCPsFromDto(demographicsDto));
            }
        } catch (EhrOperationException oe) {
            LOG.warn("Operation exception", oe);
            // TODO
        }
        return optionalHcps;
    }

    /**
     *
     * @param patientHcpDto
     * @param username
     * @param identifiedParty
     */
    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public void updatePatientHCPs(PatientHcpDto patientHcpDto, String username, IdentifiedParty identifiedParty)
            throws EhrAuthenticationException {
        try {
            IDemographicsDto demographicsDto = demographicsRecordProvider.findByLocalUsername(username);
            if (demographicsDto != null) {

                HcpDemographicsDto hcpDto = mapToHcpDemographicsDto(patientHcpDto);

                // Update record
                demographicsRecordProvider.update(demographicsDto, hcpDto, identifiedParty);
            } else {
                LOG.warn("Attempting to update HCPs for a missing demographics record");
                throw new EhrGenericClientException("Missing demographics record while trying to update HCPs");
            }

        } catch (EhrOperationException oe) {
            LOG.warn("Operation exception", oe);
            // TODO
        }
    }

    private HcpDemographicsDto mapToHcpDemographicsDto(PatientHcpDto patientHcpDto) {
        HcpDemographicsDto dto = new HcpDemographicsDto();

        if (patientHcpDto.getPrimaryCareProfessional() != null) {
            dto.setPrimaryHCPId(patientHcpDto.getPrimaryCareProfessional().getNhsId());
        }
        if (patientHcpDto.getCareProfessionals() == null) {
            patientHcpDto.setCareProfessionals(Collections.singletonList(patientHcpDto.getPrimaryCareProfessional()));
        }

        List<String> hcps = patientHcpDto.getCareProfessionals().stream().map(pd -> pd.getNhsId()).collect(Collectors.toList());
        boolean primaryInSet = hcps.stream().anyMatch(s -> s.equals(dto.getPrimaryHCPId()));
        if (!primaryInSet) {
            hcps.add(dto.getPrimaryHCPId());
        }

        dto.setHCPIds(hcps);

        return dto;
    }

}
