package com.staircase13.apperta.ehrconnector.interfaces;

import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;

/**
 * IEhrQuery provides the interface to run queries against patient electronic health records from an underlying
 *  * openEhr server. The interface is as simplified as possible, leaving details of implementing the REST or direct
 * interface of a openEhr provider to the implementation.
 */
public interface IEhrQuery {

    /**
     * Run an AQL query on the openEhr server.
     * @param aqlQuery The query to execute
     * @return Raw query results in string format
     * @throws EhrOperationException Operation exception can be thrown if the operation fails due to a downstream issue
     *        in querying the openEhr server.
     * @throws EhrAuthenticationException Authentication exception can be thrown if the user or app does not have authentication
     *        to make the downstream call to the openEhr server.
     */
    String executeBasicQuery(String aqlQuery) throws EhrOperationException, EhrAuthenticationException;

}
