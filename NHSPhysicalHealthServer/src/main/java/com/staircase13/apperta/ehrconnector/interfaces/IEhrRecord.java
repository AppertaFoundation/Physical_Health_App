package com.staircase13.apperta.ehrconnector.interfaces;

import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;

/**
 * IEhrRecord provides the interface to create and find patient electronic health records from an underlying
 * data store. The interface is as simplified as possible for the use-cases, with maximum complexity placed within the
 * record implementation.
 */
public interface IEhrRecord {

    /**
     * Find an electronic health record corresponding to an app username.
     * @param userName The app username for which to find the EHR.
     * @return The EHR details, or null if none exists.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *  in querying the openEhr server.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *  to make the downstream call to the openEhr server.
     */
    IEhrDetailsDto findEhrStatusByUserName(String userName) throws EhrOperationException, EhrAuthenticationException;

    /**
     * Create a new electronic health record corresponding to an app username.
     * @param userName The app username for which to create an EHR.
     * @param committer The committer who is responsible for creating the record.
     * @return the ehr details for the newly created record.
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *  in querying the openEhr server.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *  to make the downstream call to the openEhr server.
     */
    IEhrDetailsDto createEhrForUserName(String userName, IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;
}
