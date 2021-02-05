package com.staircase13.apperta.ehrconnector.interfaces;

import com.staircase13.apperta.ehrconnector.CompositionResult;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.service.dto.CompositionResultDto;

import java.util.List;
import java.util.Map;

/**
 * IEhrComposition provides the interface to create, find, and update compositions against patient electronic health
 * records from an underlying openEhr server.  The interface is as simplified as possible, leaving details of
 * implementing the REST or direct interface of a openEhr provider to the implementation.
 */
public interface IEhrComposition {

    /**
     * Create a new composition in the electronic health record.
     * @param ehrId The EHR id for the record in which to create the composition.
     * @param templateId The openEHR template ID for which the composition is being created.
     * @param content
     * @param committer Details of the person creating the composition.
     * @return The composition result, containing the result and the returned composition uuid.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *   in creating the composition on the openEhr server.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *   to make the downstream call to the openEhr server.
     */
    CompositionResult createComposition(String ehrId, String templateId, Map<String, List<Map<String, String>>> content,
                                       IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;

    /**
     * Fetch a specific composition by UID.
     * @param uid The UID of the composition.
     * @return Return the result of the query, containing the composition uid and raw openEHR content.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *   in querying the composition on the openEhr server.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *   to make the downstream call to the openEhr server.
     */
    CompositionResultDto fetchCompositionByUid(String uid) throws EhrOperationException, EhrAuthenticationException;

    /**
     *
     * @param ehrId The EHR id for the record for which to update the composition.
     * @param uid The UID of the composition to be updated.
     * @param templateId The openEHR template ID for which the composition is being updated.
     * @param content The openEHR content of the composition, stored in the same flat key-value pair format that is used
     *               throughout the Composition data transfer objects that are accepted in the API layer.
     * @param committer Details of the person updating the composition.
     * @return The composition result, containing the result and the returned composition uuid.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *    in updating the composition on the openEhr server.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *    to make the downstream call to the openEhr server.
     */
    CompositionResult updateComposition(String ehrId, String uid, String templateId, Map<String, List<Map<String, String>>> content,
                                        IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;
}
