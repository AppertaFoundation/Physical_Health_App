package com.staircase13.apperta.ehrconnector.interfaces;

import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.HcpDemographicsDto;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.service.dto.ProfileDto;

import java.util.List;
import java.util.Map;

/**
 * IDemographicsRecord provides the interface to create, find, update and delete demographics records to an underlying
 * data store. The interface is as simplified as possible for the use-cases, with maximum complexity placed within the
 * record implementation.
 */
public interface IDemographicsRecord {

    /**
     * Find a record using the username identifier, as used to login to a user account.
     * @param username The username for which to find a demographics record.
     * @return The demographics record or null if no record exists.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *   in querying the demographics store.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *   to make the downstream call to the demographics store.
     */
    IDemographicsDto findByLocalUsername(String username) throws EhrOperationException, EhrAuthenticationException;

    /**
     * Find a record using the id that has previously been returned for a demographics record from the demographics record
     * implementation.
     * @param remoteId The ID used to represent the record in the remote record store
     * @return The demographics record, or null if no record exists
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *  in querying the demographics store.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *  to make the downstream call to the demographics store.
     */
    IDemographicsDto findByRemoteId(long remoteId) throws EhrOperationException, EhrAuthenticationException;

    /**
     * Find a record associated with a care professional by ID and other search terms.
     * @param careProfessionalId The care professional internal id
     * @param start Start index for paged results
     * @param pageSize Page size for paged results
     * @param searchTerms a map of key-value pairs for search terms.
     * @return Matching demographics records in a IDemographicsDto
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *  in querying the demographics store.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *  to make the downstream call to the demographics store.
     */
    List<IDemographicsDto> findByHCP(String careProfessionalId, int start, int pageSize, Map<String, String> searchTerms) throws EhrOperationException, EhrAuthenticationException;

    /**
     * Create a new demographics record for a patient.
     * @param demographicsDto The demographics transfer object containing the patient information.
     * @param committer Details of the person creating the record.
     * @return The ID on the remote system of the newly created demographics record.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *  in updating the demographics store.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *  to make the downstream call to the demographics store.
     */
    long create(IDemographicsDto demographicsDto, IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;

    /**
     * Update an existing demographics record for a patient.
     * @param demographicsDto The demographics transfer object containing the patient information.
     * @param hcpDto The HCP transfer object containing details of the care professionals associated with the patient.
     * @param committer Details of the person updating the record.
     * @return true if the update is successful, false otherwise.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *  in updating the demographics store.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *  to make the downstream call to the demographics store.
     */
    boolean update(IDemographicsDto demographicsDto, HcpDemographicsDto hcpDto, IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;

    /**
     * Extract details of care professionals associated with a patient from the patient demographics record where they are stored.
     * This is handled within the concrete implementation because the method of storing the information within the record may vary
     * between systems.
     * @param demographicsDto The demographics transfer object received from the record system.
     * @return The care professionals transfer object.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to an inconsistent or missing
     *   aspect of the provided demographics record.
     */
    HcpDemographicsDto getHCPsFromDto(IDemographicsDto demographicsDto) throws EhrOperationException;

    /**
     * Extract the profile object from a demographics transfer object. This is handled within the concrete implementation
     * because the method of storing the information within the demographics record may vary between systems.
     * @param demographicsDto The demographics transfer object received from the record system.
     * @return The profile transfer object used through the client facing APIs to represent a patient.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to an inconsistent or missing
     *   aspect of the provided demographics record.
     */
    ProfileDto mapEhrToProfile(IDemographicsDto demographicsDto) throws EhrOperationException;

    /**
     * Generate a demographics transfer object to store the information in a patient profile and related care professionals objects.
     * This is handled within the concrete implementation because the method of storing the information with the demographics
     * record may vary between systems.
     * @param profileDto The profile transfer object used throughout the client facing APIs to represent a patient.
     * @param hcpDemographicsDto The care professionals transfer object representing professionals associated with a patient.
     * @param username The app username for the patient, used as a primary key for the patient centred record.
     * @return A demographics transfer object suitable for submitting to a create or update method.
     */
    IDemographicsDto mapProfileToEhr(ProfileDto profileDto, HcpDemographicsDto hcpDemographicsDto, String username);
}
