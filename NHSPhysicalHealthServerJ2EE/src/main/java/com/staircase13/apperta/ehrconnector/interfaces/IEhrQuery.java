package com.staircase13.apperta.ehrconnector.interfaces;

import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;

public interface IEhrQuery {

    String executeBasicQuery(String aqlQuery) throws EhrOperationException, EhrAuthenticationException;

}
