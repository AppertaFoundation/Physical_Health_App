package com.staircase13.apperta.ehrconnector.interfaces;

import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;

public interface IEhrRecord {

    IEhrDetailsDto findEhrStatusByUserName(String userName) throws EhrOperationException, EhrAuthenticationException;
    IEhrDetailsDto createEhrForUserName(String userName, IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;
}
