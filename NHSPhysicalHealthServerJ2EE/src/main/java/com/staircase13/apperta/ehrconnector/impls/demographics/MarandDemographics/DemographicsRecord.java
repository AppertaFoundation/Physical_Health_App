package com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics;

import com.staircase13.apperta.ehrconnector.*;
import com.staircase13.apperta.ehrconnector.impls.MarandBase.EhrBaseResponse;
import com.staircase13.apperta.ehrconnector.impls.MarandBase.EhrBase;
import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto.EhrDemographicsDto;
import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto.EhrDemographicsArrayResponseDto;
import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto.EhrDemographicsPartyResponseDto;
import com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.dto.EhrDemographicsUpdateDto;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrGenericClientException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.interfaces.IDemographicsDto;
import com.staircase13.apperta.ehrconnector.interfaces.IDemographicsRecord;
import com.staircase13.apperta.service.dto.ProfileDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.staircase13.apperta.ehrconnector.impls.demographics.MarandDemographics.MarandDemographicsRestConstants.*;
import static com.staircase13.apperta.ehrconnector.OpenEhrRestConstants.EHR_SUBJECT_NAMESPACE;

@Component
public class DemographicsRecord extends EhrBase implements IDemographicsRecord {

    private static final Logger LOG = LoggerFactory.getLogger(DemographicsRecord.class);

    private DemographicsConnector demographicsConnector;

    @Autowired
    public DemographicsRecord(DemographicsConnector demographicsConnector) {
        this.demographicsConnector = demographicsConnector;
    }

    // TODO:  findBySearch terms needed for admin/hcp?

    /**
     * Find demographics record by app defined username
     * @param username The username to search for a demographics record
     * @return
     * @throws EhrOperationException
     */
    public IDemographicsDto findByLocalUsername(String username) throws EhrOperationException, EhrAuthenticationException {

        EhrDemographicsDto demographicsDto = null;

        RestTemplate template = demographicsConnector.getSessionRestTemplate();

        UriComponents uri = demographicsConnector.getSessionUriBuilder()
                .path(DEMOGRAPHICS_PARTY_QUERY_ENDPOINT)
                .query(EHR_SUBJECT_NAMESPACE + "={appertaUsername}")
                .buildAndExpand(username);

        try {
            EhrDemographicsArrayResponseDto ehrArrayResponse = template.getForObject(uri.toUri(), EhrDemographicsArrayResponseDto.class);
            if (ehrArrayResponse != null) {

                    if (ehrArrayResponse.getParties().size() > 1) {
                        LOG.warn("Multiple demographics records for username " + username);
                    }
                    demographicsDto = ehrArrayResponse.getParties().get(0);
            } else {
                LOG.debug("No ehr demographics found for username " + username);
            }
        } catch (RestClientResponseException rre) {
            // Creation failed
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("Create composition server issue: ", rre);
            }
            // TODO:  handle getting response body and returning for failed validation
//            rre.getResponseBodyAsString()
            switch (rre.getRawStatusCode()) {
                case 403:
                    demographicsConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(rre.getRawStatusCode(), demographicsConnector.getSessionId());
                case 404:
                    throw new EhrGenericClientException("404: Create composition Operation Not Found");
                default:
                    throw new EhrOperationException("Create composition  response " + rre.getRawStatusCode() , rre);
            }
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("Create composition failed", rce);
        }
        return demographicsDto;
    }

    /**
     * Find a single demographics record by ehr party Id
     * @param partyId
     * @return
     * @throws EhrOperationException
     */
    public IDemographicsDto findByPartyId(long partyId) throws EhrOperationException, EhrAuthenticationException {
        EhrDemographicsDto demographicsDto = null;

        RestTemplate template = demographicsConnector.getSessionRestTemplate();

        UriComponents uri = demographicsConnector.getSessionUriBuilder()
                .path(DEMOGRAPHICS_PARTY_ENDPOINT + "/" + partyId).build();

        try {
            EhrDemographicsPartyResponseDto ehr = template.getForObject(uri.toUri(), EhrDemographicsPartyResponseDto.class);
            demographicsDto = ehr.getParty();
        } catch (RestClientResponseException rre) {
            // Creation failed
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("Demographics findByParty server issue: ", rre);
            }
            // TODO:  handle getting response body and returning for failed validation
//            rre.getResponseBodyAsString()
            switch (rre.getRawStatusCode()) {
                case 403:
                    demographicsConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(rre.getRawStatusCode(), demographicsConnector.getSessionId());
                case 404:
                    // 404 means the record doesn't exist
                default:
                    throw new EhrOperationException("Create composition  response " + rre.getRawStatusCode() , rre);
            }
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("");
        }

        return demographicsDto;
    }

    /**
     * Get a list of demographics records which are associated with an HCP id
     * @param careProfessionalId
     * @return
     * @throws EhrOperationException
     */
    public List<IDemographicsDto> findByHCP(String careProfessionalId) throws EhrOperationException, EhrAuthenticationException {
        List<IDemographicsDto> results = new ArrayList<>();

        RestTemplate template = demographicsConnector.getSessionRestTemplate();

        UriComponents uri = demographicsConnector.getSessionUriBuilder()
                .path(DEMOGRAPHICS_PARTY_QUERY_ENDPOINT)
                .query(EHR_HEALTHCARE_PROFESSIONAL_KEY + "={hcpId}")
                .buildAndExpand(careProfessionalId);

        try {
            EhrDemographicsArrayResponseDto ehr = template.getForObject(uri.toUri(), EhrDemographicsArrayResponseDto.class);
            if (ehr != null) {
                results = Collections.unmodifiableList(ehr.getParties());
            }
        } catch (RestClientResponseException rre) {
            // Creation failed
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("Demographics findByHCP server issue: ", rre);
            }
            // TODO:  handle getting response body and returning for failed validation
//            rre.getResponseBodyAsString()
            switch (rre.getRawStatusCode()) {
                case 403:
                    demographicsConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(rre.getRawStatusCode(), demographicsConnector.getSessionId());
                case 404:
                    // 404 means the record doesn't exist
                default:
                    throw new EhrOperationException("Create composition  response " + rre.getRawStatusCode() , rre);
            }
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("Ehr Error response: " + rce.getMessage());
        }

        return results;
    }

    /**
     * Create a new demographics record
     * @param demographicsDto
     * @return the party id upon success, -1 otherwise
     * @throws EhrOperationException
     */
    public long create(IDemographicsDto demographicsDto, IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException {

        if (!(demographicsDto instanceof EhrDemographicsDto)) {
            throw new EhrGenericClientException("Demographics module cannot map from the provided dto");
        }
        EhrDemographicsDto ehrDemographicsDto = (EhrDemographicsDto) demographicsDto;

        long resultPartyId = -1;
        RestTemplate template = demographicsConnector.getSessionRestTemplate();

        UriComponents uri = demographicsConnector.getSessionUriBuilder()
                .path(DEMOGRAPHICS_PARTY_ENDPOINT)
                .query("committerName={committerName}&committerId={committerId}")
                .buildAndExpand(committer.getName(), committer.getNumber());

        try {
            EhrBaseResponse response = template.postForObject(uri.toUri(), ehrDemographicsDto, EhrBaseResponse.class);
            if (response.getAction().equalsIgnoreCase(OpenEhrRestConstants.RESPONSE_ACTION_CREATE)){
                resultPartyId = getIdFromHref(response);
            }
        } catch (RestClientResponseException rce) {
            int statusCode = rce.getRawStatusCode();
            String errorResponse = rce.getResponseBodyAsString();

            // TODO
            rce.printStackTrace();
        }
        return  resultPartyId;
    }

    /**
     * Update a demographics record
     * @param demographicsDto Demographics Dto to use for update
     * @param hcpDto HCPs Dto to use for update
     * @param committer Committer identity to use for update
     * @return Boolean true if update was successful
     * @throws EhrOperationException
     */
    public boolean update(IDemographicsDto demographicsDto, HcpDemographicsDto hcpDto,
                          IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException {

        if (!(demographicsDto instanceof EhrDemographicsDto)) {
            throw new EhrGenericClientException("Demographics module cannot map from the provided dto");
        }
        EhrDemographicsDto ehrDemographicsDto = (EhrDemographicsDto) demographicsDto;

        if (ehrDemographicsDto.getPartyId() < 0) throw new IllegalArgumentException("Demographics record cannot be updated without id");

        // copy HCP mapping
        EhrDemographicsUpdateDto updateDto = copyHealthProviderMapping(ehrDemographicsDto,hcpDto);

        boolean success = false;
        RestTemplate template = demographicsConnector.getSessionRestTemplate();

        UriComponents uri = demographicsConnector.getSessionUriBuilder()
                .path(DEMOGRAPHICS_PARTY_ENDPOINT)
                .query("committerName={committerName}&committerId={committerId}")
                .buildAndExpand(committer.getName(), committer.getNumber());

        try {
            template.put(uri.toUri(), updateDto);
            success = true;
        } catch (RestClientResponseException rre) {
            // Creation failed
            int statusCode = rre.getRawStatusCode();
            if (statusCode >= 500 && statusCode < 600) {
                throw new EhrOperationException("Demographics update server issue: ", rre);
            }
            // TODO:  handle getting response body and returning for failed validation
//            rre.getResponseBodyAsString()
            switch (rre.getRawStatusCode()) {
                case 403:
                    demographicsConnector.resetSessionRestTemplate();
                    // intentionally drops through
                case 401:
                    throw new EhrAuthenticationException(rre.getRawStatusCode(), demographicsConnector.getSessionId());
                case 404:
                    throw new EhrGenericClientException("404: Create composition Operation Not Found");
                default:
                    throw new EhrOperationException("Create composition  response " + rre.getRawStatusCode() , rre);
            }
        } catch (RestClientException rce) {
            throw new EhrGenericClientException("");
        }
        return success;
    }

    public HcpDemographicsDto getHCPsFromDto(IDemographicsDto demographicsDto) throws EhrOperationException {

        if (!(demographicsDto instanceof EhrDemographicsDto)) {
            throw new EhrGenericClientException("Demographics module cannot map from the provided dto");
        }
        EhrDemographicsDto ehrDemographicsDto = (EhrDemographicsDto) demographicsDto;

        String primaryCareProfId = null;
        ArrayList<String> careProfIds = new ArrayList<>();
        for (EhrDemographicsDto.AdditionalInfo info : ehrDemographicsDto.getPartyAdditionalInfo()) {
            String key = info.getKey();
            if (key.equals(EHR_PRIMARY_HEALTHCARE_PROFESSIONAL_KEY)) {
                primaryCareProfId = info.getValue();
            } else if (key.equals(EHR_HEALTHCARE_PROFESSIONAL_KEY)) {
                careProfIds.add(info.getValue());
            }
        }
        return HcpDemographicsDto.builder().primaryHCPId(primaryCareProfId).HCPIds(careProfIds).build();
    }

    /**
     * Map ehr to profile dto which can be used in client api
     * @param demographicsDto
     * @return
     */
    public ProfileDto mapEhrToProfile(IDemographicsDto demographicsDto) throws EhrOperationException {

        if (!(demographicsDto instanceof EhrDemographicsDto)) {
            throw new EhrGenericClientException("Demographics module cannot map from the provided dto");
        }
        EhrDemographicsDto ehrDemographicsDto = (EhrDemographicsDto) demographicsDto;

        String username = null;
        String mobile = null;
        String tel = null;
        String nhs = null;
        String title = null;
        int genderCode = -1;
        String primaryCareProfId = null;
        ArrayList<String> careProfIds = new ArrayList<>();
        for (EhrDemographicsDto.AdditionalInfo info : ehrDemographicsDto.getPartyAdditionalInfo()) {
            String key = info.getKey();
            if (key.equals(EHR_SUBJECT_NAMESPACE)) {
                username = info.getValue();
            } else if (key.equals(EHR_MOBILE_TELEPHONE)) {
                mobile = info.getValue();
            } else if (key.equals(EHR_TELEPHONE)) {
                tel = info.getValue();
            } else if (key.equals(EHR_TITLE)) {
                title = info.getValue();
            } else if (key.equals(EHR_NHS_NUMBER)) {
                nhs = info.getValue();
            } else if (key.equals(EHR_GENDER_CODE)) {
                genderCode = Integer.parseInt(info.getValue());
            } else if (key.equals(EHR_PRIMARY_HEALTHCARE_PROFESSIONAL_KEY)) {
                primaryCareProfId = info.getValue();
            } else if (key.equals(EHR_HEALTHCARE_PROFESSIONAL_KEY)) {
                careProfIds.add(info.getValue());
            }
        }

        String address = null;
        if (ehrDemographicsDto.getAddress() != null) { // address not mandatory
            address = ehrDemographicsDto.getAddress().getAddress();
        }

        HcpDemographicsDto hcps = HcpDemographicsDto.builder().primaryHCPId(primaryCareProfId).HCPIds(careProfIds).build();

        return ProfileDto.builder()
                .username(username)
                .firstNames(ehrDemographicsDto.getFirstNames())
                .lastName(ehrDemographicsDto.getLastNames())
                .dateOfBirth(ehrDemographicsDto.getDateOfBirth())
                .address(address)
                .mobileNumber(mobile)
                .telNumber(tel)
                .gender(ehrDemographicsDto.getGender())
                .genderCode(genderCode)
                .nhsNumber(nhs)
                .title(title)
                .hcps(hcps)
                .build();
    }

    /**
     * Map client profile dto to ehr format required for demographics api
     * @param profileDto
     * @return
     */
    public IDemographicsDto mapProfileToEhr(ProfileDto profileDto, HcpDemographicsDto hcpDemographicsDto, String username) {

        List<EhrDemographicsDto.AdditionalInfo> additionalInfo = new ArrayList<>();
        additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(OpenEhrRestConstants.EHR_SUBJECT_NAMESPACE, username));
        if (profileDto.getTitle() != null) {
            additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_TITLE, profileDto.getTitle()));
        }
        if (profileDto.getNhsNumber() != null) {
            additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_NHS_NUMBER, profileDto.getNhsNumber()));
        }
        if (profileDto.getMobileNumber() != null) {
            additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_MOBILE_TELEPHONE, profileDto.getMobileNumber()));
        }
        if (profileDto.getTelNumber() != null) {
            additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_TELEPHONE, profileDto.getTelNumber()));
        }
        if (profileDto.getGenderCode() >= 0) {
            additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_GENDER_CODE, Integer.toString(profileDto.getGenderCode())));
        }
        if (hcpDemographicsDto != null){
            if (hcpDemographicsDto.getPrimaryHCPId() != null) {
                additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_PRIMARY_HEALTHCARE_PROFESSIONAL_KEY, hcpDemographicsDto.getPrimaryHCPId()));
                additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_HEALTHCARE_PROFESSIONAL_KEY, hcpDemographicsDto.getPrimaryHCPId()));
            }
            hcpDemographicsDto.getHCPIds().stream().forEach(h -> additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_HEALTHCARE_PROFESSIONAL_KEY, h)));
        }

        EhrDemographicsDto dto = EhrDemographicsDto.builder()
                .firstNames(profileDto.getFirstNames())
                .lastNames(profileDto.getLastName())
                // TODO: validate date format
                .dateOfBirth(profileDto.getDateOfBirth())
                // TODO: validate gender formatting
                .gender(profileDto.getGender().toUpperCase())
                .address(new EhrDemographicsDto.EhrAddress(profileDto.getAddress()))
                .partyAdditionalInfo(additionalInfo)
                .build();
        return dto;
    }

    /**
     * Demographics details and HCP mappings are stored in the demographics record. When updating
     * only the basic demographics we do not want to change the HCP records, so they are copied across
     * from the old record. This method merges the records to use one profile and one HCP.
     * @param profileDemographics The profile to use
     * @param hcpDemographicsDto The hcp to use
     * @return The merged record
     */
    private EhrDemographicsUpdateDto copyHealthProviderMapping(EhrDemographicsDto profileDemographics,
                                                         HcpDemographicsDto hcpDemographicsDto) {

        // remove all additional infos which are for hcps from profileDemograpics
        ArrayList<EhrDemographicsDto.AdditionalInfo> additionalInfo = profileDemographics.getPartyAdditionalInfo().stream()
                .filter(i -> (!i.getKey().equals(EHR_HEALTHCARE_PROFESSIONAL_KEY) && (!i.getKey().equals(EHR_PRIMARY_HEALTHCARE_PROFESSIONAL_KEY))))
                .collect(Collectors.toCollection(ArrayList::new));

        // find all additional infos which are for hcps in hcpDemographicsDto
        if (hcpDemographicsDto != null){
            if (hcpDemographicsDto.getPrimaryHCPId() != null) {
                additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_PRIMARY_HEALTHCARE_PROFESSIONAL_KEY, hcpDemographicsDto.getPrimaryHCPId()));
            }
            hcpDemographicsDto.getHCPIds().stream().forEach(h -> additionalInfo.add(new EhrDemographicsDto.AdditionalInfo(EHR_HEALTHCARE_PROFESSIONAL_KEY, h)));
        }

        profileDemographics.setPartyAdditionalInfo(additionalInfo);

        return new EhrDemographicsUpdateDto(profileDemographics);
    }

}
