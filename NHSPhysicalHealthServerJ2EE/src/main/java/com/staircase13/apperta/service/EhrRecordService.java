package com.staircase13.apperta.service;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.staircase13.apperta.ehrconnector.interfaces.IEhrComposition;
import com.staircase13.apperta.ehrconnector.interfaces.IEhrQuery;
import com.staircase13.apperta.ehrconnector.interfaces.IEhrDetailsDto;
import com.staircase13.apperta.ehrconnector.interfaces.IEhrRecord;
import com.staircase13.apperta.ehrconnector.CompositionResult;
import com.staircase13.apperta.ehrconnector.ConfigConstants;
import com.staircase13.apperta.ehrconnector.IdentifiedParty;
import com.staircase13.apperta.ehrconnector.exception.EhrAuthenticationException;
import com.staircase13.apperta.ehrconnector.exception.EhrOperationException;
import com.staircase13.apperta.ehrconnector.exception.EhrServerException;
import com.staircase13.apperta.ehrconnector.exception.HcpMissingDetailsException;
import com.staircase13.apperta.entities.CompositionParameters;
import com.staircase13.apperta.entities.QueryTemplate;
import com.staircase13.apperta.repository.CompositionParametersRepository;
import com.staircase13.apperta.repository.QueryTemplateRespository;
import com.staircase13.apperta.service.dto.*;
import com.staircase13.apperta.service.exception.AppertaException;
import com.staircase13.apperta.service.exception.AppertaJsonException;
import com.staircase13.apperta.service.exception.InvalidParameterMapException;
import com.staircase13.apperta.service.exception.InvalidQueryTemplateException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.env.Environment;
import org.springframework.lang.Nullable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Validator;

import java.io.StringReader;
import java.io.StringWriter;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.staircase13.apperta.ehrconnector.OpenEhrRestConstants.*;
import static com.staircase13.apperta.service.util.DateTimeUtil.toLocalDateTime;
import static java.time.format.DateTimeFormatter.ISO_DATE_TIME;

@Service
public class EhrRecordService {

    private static final Logger LOG = LoggerFactory.getLogger(EhrRecordService.class);

    /** The connection for ehr record management */
    private IEhrRecord ehrRecordProvider;

    /** The connection for reading or writing compositions to the ehr */
    private IEhrComposition ehrCompositionProvider;

    private IEhrQuery ehrQueryProvider;

    private final CompositionParametersRepository parametersRepository;

    private final QueryTemplateRespository queryTemplateRespository;

    private final IdentifiedParty  patientParty;

    private final Validator validator;

    private final DateTimeFormatter formatter;

    private final MustacheFactory mustacheFactory;

    private final int defaultPageSize;

    @Autowired
    public EhrRecordService(IEhrRecord ehrRecordProvider, IEhrComposition ehrComposition, IEhrQuery ehrQuery,
                            CompositionParametersRepository parametersRepository,  QueryTemplateRespository queryTemplateRespository,
                            Validator validator, Environment environment) {
        this.ehrRecordProvider = ehrRecordProvider;
        this.ehrCompositionProvider = ehrComposition;
        this.ehrQueryProvider = ehrQuery;
        this.parametersRepository = parametersRepository;
        this.queryTemplateRespository = queryTemplateRespository;
        this.validator = validator;
        formatter = ISO_DATE_TIME;
        mustacheFactory = new DefaultMustacheFactory();

        String patientCommitterName = environment.getProperty(ConfigConstants.PATIENT_COMMITTER_NAME);
        String patientCommitterNumber = environment.getProperty(ConfigConstants.PATIENT_COMMITTER_NUMBER);
        patientParty = IdentifiedParty.builder()
                .name(patientCommitterName)
                .number(patientCommitterNumber)
                .build();

        int pagesize = 50;
        try {
            pagesize = Integer.parseInt(environment.getProperty(ConfigConstants.DEFAULT_QUERY_PAGE_SIZE));
        } catch (NumberFormatException nfe) {
            LOG.warn("Missing valid default query page size");
        }
        defaultPageSize = pagesize;
    }

    @Cacheable("ehrRecordSession")
    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public EhrRecordSession findOrCreateEhr(String username, @Nullable IdentifiedParty identifiedParty)
                throws EhrAuthenticationException {

        EhrRecordSession session = null;
        try {
            IEhrDetailsDto ehrDetails = ehrRecordProvider.findEhrStatusByUserName(username);

            if (ehrDetails == null) {
                // Could not find existing record so create
                if (identifiedParty == null) {
                    identifiedParty = patientParty;
                }

                ehrDetails = ehrRecordProvider.createEhrForUserName(username, identifiedParty);
            }

            if (ehrDetails != null) {
                session = new EhrRecordSession(username, ehrDetails.getEhrId());
            }

        } catch (EhrOperationException oe) {

        }
        return session;
    }

    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public PermissionsDto getConsents(EhrRecordSession ehrRecordSession) {

        return null;
    }

    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public PermissionsDto updateConsents(PermissionsDto permissionsDTO, EhrRecordSession ehrRecordSession) {

        return null;
    }

    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public void createComposition(EhrRecordSession ehrRecordSession, CompositionDto compositionDto, IdentifiedParty identifiedParty)
                throws EhrOperationException, EhrAuthenticationException {


        Map<String, List<Map<String, String>>> contents = compositionDto.getContents();

        // Add default context fields if missing
        checkRequiredCompositionContextFields(contents, identifiedParty, compositionDto.getRecordDate());

        CompositionResult compositionResult = ehrCompositionProvider.createComposition(ehrRecordSession.getEhrId(),
                compositionDto.getEhrTemplateName(), contents, identifiedParty);
        if (!compositionResult.isSuccess()) {
            // Throw
        }
    }

    public void createTemplatedComposition(EhrRecordSession ehrRecordSession, CompositionDto compositionDto,
                                           IdentifiedParty identifiedParty) throws InvalidParameterMapException,
            EhrOperationException, EhrAuthenticationException  {

        List<CompositionParameters> parametersLookup = parametersRepository.findBySetName(compositionDto.getParameterMap());

        if (parametersLookup.isEmpty()) throw new InvalidParameterMapException(compositionDto.getParameterMap());

        Map<String, List<Map<String, String>>> contents = compositionDto.getContents();
        parametersLookup.forEach(parameter -> {
            checkContentsForParameter(contents, parameter);
            contents.keySet().forEach(prefix -> {
                List<Map<String, String>> valuesList = contents.get(prefix);
                valuesList.forEach(content -> checkItemContentsForParameter(content, parameter));
            });
        });

        createComposition(ehrRecordSession, compositionDto, identifiedParty);
    }

    private void checkContentsForParameter(Map<String, List<Map<String, String>>> contents, CompositionParameters parameter) {
        // Map parameterName  -> ehrName
        List<Map<String, String>> value = contents.remove(parameter.getParameterName());
        if (value != null){
            contents.put(parameter.getEhrName(), value);
        }
    }

    private void checkItemContentsForParameter(Map<String, String> contents, CompositionParameters parameter) {
        // Map parameterName  -> ehrName
        String value = contents.remove(parameter.getParameterName());
        if (value != null){
            contents.put(parameter.getEhrName(), value);
        }
    }

    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public CompositionResultDto getCompositionByUid(EhrRecordSession ehrSession, String compositionUid)
            throws EhrAuthenticationException, EhrOperationException {
        return ehrCompositionProvider.fetchCompositionByUid(compositionUid);
    }

    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public void updateComposition(EhrRecordSession ehrRecordSession, String compositionUid,
                                  CompositionDto compositionDto, IdentifiedParty identifiedParty)
            throws EhrAuthenticationException, EhrOperationException {
        // Add default fields if missing
        Map<String, List<Map<String, String>>> content = compositionDto.getContents();
        checkRequiredCompositionContextFields(content, identifiedParty, compositionDto.getRecordDate());

        CompositionResult compositionResult = ehrCompositionProvider.updateComposition(ehrRecordSession.getEhrId(),
                compositionUid, compositionDto.getEhrTemplateName(), content, identifiedParty);
        if (!compositionResult.isSuccess()) {
            // Throw
        }
    }

    public void updateTemplatedComposition(EhrRecordSession ehrRecordSession, String compositionUid,
                                           CompositionDto compositionDto, IdentifiedParty identifiedParty)
            throws EhrAuthenticationException, EhrOperationException, InvalidParameterMapException {
        List<CompositionParameters> parametersLookup = parametersRepository.findBySetName(compositionDto.getParameterMap());

        if (parametersLookup.isEmpty()) throw new InvalidParameterMapException(compositionDto.getParameterMap());

        Map<String, List<Map<String, String>>> contents = compositionDto.getContents();
        parametersLookup.forEach(parameter -> {
            checkContentsForParameter(contents, parameter);
            contents.keySet().forEach(prefix -> {
                List<Map<String, String>> valuesList = contents.get(prefix);
                valuesList.forEach(content -> checkItemContentsForParameter(content, parameter));
            });
        });

        updateComposition(ehrRecordSession, compositionUid, compositionDto, identifiedParty);
    }

    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    public EhrQueryResultDto executeDirectQuery(EhrRecordSession ehrRecordSession, EhrQueryDto ehrQueryDto)
            throws AppertaException {

        List<String> selectColumns = ehrQueryDto.getSelectColumns();
        List<String> whereclauses = ehrQueryDto.getWhereClauses();
        Map<String, String> errorMap = new HashMap<>();
        if (selectColumns == null || selectColumns.size() == 0) {
            errorMap.put("selectColumns","is empty");
        }
        if (whereclauses == null || whereclauses.size() == 0) {
            errorMap.put("whereClauses","is empty");
        }
        if (StringUtils.isEmpty(ehrRecordSession.getEhrId())) {
            errorMap.put("ehr","could not be found");
        }

        if (!errorMap.isEmpty()) {
            // TODO: throw appropriate exception type with validation message
            throw new AppertaJsonException(1,errorMap);
        }
        // select
        StringBuilder sb = new StringBuilder("select ");

        // e/ehr_id/value as ehrId, a/uid/value as compositionId, a/composer/primaryEntityName as composerName, b_a/time as time, b_a/Systolic/magnitude as systolic, b_a/Systolic/unit, b_a/Diastolic/magnitude as diastolic
        sb.append(selectColumns.stream().collect(Collectors.joining(", ")));

        // from EHR e
        sb.append(" from EHR e ");

        // contains COMPOSITION a[openEHR-EHR-COMPOSITION.encounter.v1]
        // contains OBSERVATION b_a[openEHR-EHR-OBSERVATION.blood_pressure.v1]

        sb.append(whereclauses.stream().collect(Collectors.joining(" ")));

        // check where clauses to see if any contain "where"
        boolean containsWhere = whereclauses.stream().anyMatch(c -> c.contains("where"));
        sb.append(containsWhere ? " and " : " where ");
        // where e/ehr_id/value='6f29ba67-fc44-423e-9417-d6bc1196a06e'
        // and
        sb.append("e/ehr_id/value='").append(ehrRecordSession.getEhrId()).append("'");

        // TODO:  handling search time ranges
        if (ehrQueryDto.getStartDateTime() > 0 || ehrQueryDto.getEndDateTime() > 0) {

        }

        if (ehrQueryDto.getStart() > 0 || ehrQueryDto.getPagesize() > 0) {
            int start = ehrQueryDto.getStart() <= 0 ? 0 : ehrQueryDto.getStart();
            int size = ehrQueryDto.getPagesize() > 0 ? ehrQueryDto.getPagesize() : defaultPageSize;
            sb.append(" offset ").append(start).append(" limit ").append(size);
        }

        String resultSet = ehrQueryProvider.executeBasicQuery(sb.toString());
        EhrQueryResultDto resultDto = new EhrQueryResultDto();
        resultDto.setResultSet(resultSet);
        resultDto.setPagesize(ehrQueryDto.getPagesize() > 0 ? ehrQueryDto.getPagesize() :  0);
        resultDto.setStart(ehrQueryDto.getStart() >= 0 ? ehrQueryDto.getStart() : 0);

        return resultDto;
    }

    @Retryable(
            value = { EhrAuthenticationException.class, EhrServerException.class},
            maxAttempts = 3,
            backoff = @Backoff(delay = 200) )
    @Transactional
    public EhrQueryResultDto executeTemplatedQuery(EhrRecordSession ehrRecordSession, EhrQueryDto ehrQueryDto)
            throws EhrAuthenticationException, EhrOperationException, InvalidParameterMapException,
            InvalidQueryTemplateException {

        List<QueryTemplate> queryTemplates = queryTemplateRespository.findByName(ehrQueryDto.getQueryTemplate());
        if (queryTemplates.isEmpty()) throw new InvalidQueryTemplateException(ehrQueryDto.getQueryTemplate());

        Map<String, String> parameters = ehrQueryDto.getParameters();
        if (ehrQueryDto.getParameterMap() != null && parameters != null && parameters.size() > 0) {
            List<CompositionParameters> parametersLookup = parametersRepository.findBySetName(ehrQueryDto.getParameterMap());
            if (parametersLookup.isEmpty()) throw new InvalidParameterMapException(ehrQueryDto.getParameterMap());

            parametersLookup.forEach(parameter -> checkItemContentsForParameter(parameters, parameter));
        }

        StringBuffer sb = null;

        // Template replacement
        if (parameters != null && parameters.size() > 0) {
            sb = fillTemplate(queryTemplates.get(0).getTemplate(), ehrQueryDto.getQueryTemplate(), parameters);
        } else {
            sb = new StringBuffer(queryTemplates.get(0).getTemplate());
        }

        // Add where clause for ehrid
        boolean containsWhere = sb.indexOf(" where ") >= 0;

        StringBuffer whereClause = new StringBuffer();
        whereClause.append(containsWhere ? " and " : " where ");
        whereClause.append("e/ehr_id/value='").append(ehrRecordSession.getEhrId()).append("'");

        int orderByIndex = sb.indexOf(" order by ");
        if (orderByIndex > 0) {
            sb.insert(orderByIndex, whereClause);
        } else {
           sb.append(whereClause);
        }

        // TODO:  handling search time ranges
        if (ehrQueryDto.getStartDateTime() > 0 || ehrQueryDto.getEndDateTime() > 0) {

        }

        if (ehrQueryDto.getStart() > 0 || ehrQueryDto.getPagesize() > 0) {
            int start = ehrQueryDto.getStart() <= 0 ? 0 : ehrQueryDto.getStart();
            int size = ehrQueryDto.getPagesize() > 0 ? ehrQueryDto.getPagesize() : defaultPageSize;
            sb.append(" offset ").append(start).append(" limit ").append(size);
        }

        String resultSet = ehrQueryProvider.executeBasicQuery(sb.toString());
        EhrQueryResultDto resultDto = new EhrQueryResultDto();
        resultDto.setResultSet(resultSet);
        resultDto.setPagesize(ehrQueryDto.getPagesize() > 0 ? ehrQueryDto.getPagesize() :  0);
        resultDto.setStart(ehrQueryDto.getStart() >= 0 ? ehrQueryDto.getStart() : 0);

        return resultDto;
    }

    @Transactional
    public MultiQueryResultDto executeMultipleQueries(EhrRecordSession ehrRecordSession, MultiEhrQueryDto multiQueryDto)
            throws EhrAuthenticationException, EhrOperationException, AppertaException {

        HashMap<String, EhrQueryResultDto> results = new HashMap<>();
        MultiQueryResultDto resultDto = new MultiQueryResultDto(results);

        Set<String> queryKeys = multiQueryDto.getQueries().keySet();

        for (String key : queryKeys) {
            EhrQueryDto queryDto = multiQueryDto.getQueries().get(key);
            // run this within the loop to catch these two types of exceptions and return them as error messages within the results array
            EhrQueryResultDto queryResultDto = null;
            try {
                if (queryDto.getParameterMap() != null || queryDto.getQueryTemplate() != null) {
                    queryResultDto = executeTemplatedQuery(ehrRecordSession, queryDto);
                } else {
                    queryResultDto = executeDirectQuery(ehrRecordSession, queryDto);
                }
            } catch (InvalidParameterMapException paramMapException) {
                queryResultDto = new EhrQueryResultDto("{\"Error\" : \"" + paramMapException.getMessage() + "\"}",0,0);
            } catch (InvalidQueryTemplateException queryTemplateException) {
                queryResultDto = new EhrQueryResultDto("{\"Error\" : \"" + queryTemplateException.getMessage() + "\"}",0,0);
            }
            results.put(key, queryResultDto);
        }

        return resultDto;
    }

    private StringBuffer fillTemplate(String template, String templateName, Map<String, String> parameters) {
        StringWriter writer = new StringWriter();

        Mustache mustache = mustacheFactory.compile(new StringReader(template), templateName);
        mustache.execute(writer, parameters);

        return writer.getBuffer();
    }

    public IdentifiedParty createIdentifiedPartyFromHCP(HcpDto hcpDto) throws HcpMissingDetailsException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(hcpDto, "HCP");
        validator.validate(hcpDto, bindingResult);
        if (bindingResult.hasErrors()) throw new HcpMissingDetailsException(hcpDto.getUsername());

        StringBuilder name = new StringBuilder(hcpDto.getTitle()).append(" ").append(hcpDto.getFirstNames())
                .append(" ").append(hcpDto.getLastName());
        return IdentifiedParty.builder().name(name.toString()).number(hcpDto.getNhsId()).build();
    }

    public IdentifiedParty getPatientIdentifiedParty() {
        return patientParty;
    }

    private void checkRequiredCompositionContextFields(Map<String, List<Map<String,String>>> composition, IdentifiedParty committer, long recordDate) {
        Map<String, String> contextBlock;
        if (composition.keySet().contains(CONTEXT_PREFIX)){
            contextBlock = composition.get(CONTEXT_PREFIX).get(0);
        } else {
            contextBlock = new HashMap<>();
            composition.put(CONTEXT_PREFIX, Collections.singletonList(contextBlock));
        }

        checkRequiredCompositionField(contextBlock, CONTEXT_LANGUAGE, "en");
        checkRequiredCompositionField(contextBlock, CONTEXT_TERRITORY, "GB");
        checkRequiredCompositionField(contextBlock, CONTEXT_COMPOSER_NAME, committer.getName());
        checkRequiredCompositionField(contextBlock, CONTEXT_TIME, toLocalDateTime(recordDate).format(formatter));
//        checkRequiredCompositionField(composition, CONTEXT_ID_NAMESPACE, ""); // TODO: add to committer ??
//        checkRequiredCompositionField(composition, CONTEXT_ID_SCHEME, "");
//        checkRequiredCompositionField(composition, CONTEXT_FACILITY, "");
//        checkRequiredCompositionField(composition, CONTEXT_FACILITY_ID, "");
    }

    private void checkRequiredCompositionField(Map<String, String> composition, String fieldname, String value) {
        if (!composition.containsKey(fieldname)) {
            composition.put(fieldname, value);
        }
    }
}
