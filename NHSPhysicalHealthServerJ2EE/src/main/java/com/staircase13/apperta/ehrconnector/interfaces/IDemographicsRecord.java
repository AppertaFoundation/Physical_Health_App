package com.staircase13.apperta.ehrconnector.interfaces;

import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.HcpDemographicsDto;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.service.dto.ProfileDto;

import java.util.List;

public interface IDemographicsRecord {


    IDemographicsDto findByLocalUsername(String username) throws EhrOperationException, EhrAuthenticationException;
    IDemographicsDto findByPartyId(long partyId) throws EhrOperationException, EhrAuthenticationException;
    List<IDemographicsDto> findByHCP(String careProfessionalId) throws EhrOperationException, EhrAuthenticationException;
    long create(IDemographicsDto demographicsDto, IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;
    boolean update(IDemographicsDto demographicsDto, HcpDemographicsDto hcpDto, IdentifiedParty committer) throws EhrOperationException, EhrAuthenticationException;
    HcpDemographicsDto getHCPsFromDto(IDemographicsDto demographicsDto) throws EhrOperationException;
    ProfileDto mapEhrToProfile(IDemographicsDto demographicsDto) throws EhrOperationException;
    IDemographicsDto mapProfileToEhr(ProfileDto profileDto, HcpDemographicsDto hcpDemographicsDto, String username);
}
