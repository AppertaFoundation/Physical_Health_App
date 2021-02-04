package com.staircase13.apperta.ehrconnector.interfaces;

import com.staircase13.apperta.ehrconnector.CompositionResult;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.service.dto.CompositionResultDto;

import java.util.List;
import java.util.Map;

public interface IEhrComposition {

    CompositionResult createComposition(String ehrId, String templateId, Map<String, List<Map<String, String>>> content,
                                       IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;

    CompositionResultDto fetchCompositionByUid(String uid) throws EhrOperationException, EhrAuthenticationException;

    CompositionResult updateComposition(String ehrId, String uid, String templateId, Map<String, List<Map<String, String>>> content,
                                        IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;
}
