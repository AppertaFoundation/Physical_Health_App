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
import com.staircase13.apperta.service.exception.PatientUsernameNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EhrDemographicsService {

    private static final Logger LOG = LoggerFactory.getLogger(EhrDemographicsService.class);

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
    public Optional<ProfileDto> findProfileByUsername(String username) throws EhrAuthenticationException, EhrOperationException {
        LOG.debug("Find profile by username [{}]",username);

        Optional<ProfileDto> profileResult = Optional.empty();
           IDemographicsDto demographicsDto = demographicsRecordProvider.findByLocalUsername(username);

            if (demographicsDto != null) {
                profileResult = Optional.of(demographicsRecordProvider.mapEhrToProfile(demographicsDto));
            }
        return profileResult;
    }

    /**
     * Find profiles which are associated with the HCP
     * @param careProfessionalId The care professional id for which associated patient records are searched
     * @param start the start index for paged results
     * @param pageSize the page size for paged results
     * @param searchTerms A map of key value pairs for searching
     * @return The profiles of patients matching the search requirements
     */
    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public List<ProfileDto> findProfilesByHCPId(String careProfessionalId, int start, int pageSize, Map<String, String> searchTerms) throws EhrAuthenticationException, EhrOperationException {
        List<ProfileDto> profiles = new ArrayList<>();

        List<IDemographicsDto> hcpDemographicsList = demographicsRecordProvider.findByHCP(careProfessionalId, start, pageSize, searchTerms);

        for(IDemographicsDto demographicsDto : hcpDemographicsList){
            profiles.add(demographicsRecordProvider.mapEhrToProfile(demographicsDto));
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
    @CacheEvict(value = "ehrDemographicsProfile", key = "#username")
    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public Optional<ProfileDto> updateOrCreateEhr(ProfileDto profileDto, String username, IdentifiedParty identifiedParty)
            throws EhrAuthenticationException, EhrOperationException {

        Optional<ProfileDto> profileResult = Optional.empty();
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
                    demographicsDto = demographicsRecordProvider.findByRemoteId(partyId);
                }
            }

            if (demographicsDto != null) {
                profileResult = Optional.of(demographicsRecordProvider.mapEhrToProfile(demographicsDto));
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
    public Optional<HcpDemographicsDto> getNHSIdsForPatientHCPs(String username)
            throws EhrAuthenticationException, EhrOperationException, PatientUsernameNotFoundException {

       Optional<HcpDemographicsDto> optionalHcps = Optional.empty();
            IDemographicsDto demographicsDto = demographicsRecordProvider.findByLocalUsername(username);

            if (demographicsDto != null) {
                optionalHcps =  Optional.of(demographicsRecordProvider.getHCPsFromDto(demographicsDto));
            } else {
                throw new PatientUsernameNotFoundException(username);
            }


        return optionalHcps;
    }

    /**
     *
     * @param patientHcpDto
     * @param username
     * @param identifiedParty
     */
    @CacheEvict(value = "ehrDemographicsProfile", key = "#username")
    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public void updatePatientHCPs(PatientHcpDto patientHcpDto, String username, IdentifiedParty identifiedParty)
            throws EhrAuthenticationException, EhrOperationException {
            IDemographicsDto demographicsDto = demographicsRecordProvider.findByLocalUsername(username);
            if (demographicsDto != null) {

                HcpDemographicsDto hcpDto = mapToHcpDemographicsDto(patientHcpDto);

                // Update record
                demographicsRecordProvider.update(demographicsDto, hcpDto, identifiedParty);
            } else {
                LOG.warn("Attempting to update HCPs for a missing demographics record");
                throw new EhrGenericClientException("Missing demographics record while trying to update HCPs");
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
