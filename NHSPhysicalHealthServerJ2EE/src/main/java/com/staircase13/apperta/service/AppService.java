package com.staircase13.apperta.service;

import com.staircase13.apperta.entities.*;
import com.staircase13.apperta.repository.CompositionParametersRepository;
import com.staircase13.apperta.repository.DeviceAppRepository;
import com.staircase13.apperta.repository.QueryTemplateRespository;
import com.staircase13.apperta.repository.TemplateRepository;
import com.staircase13.apperta.service.dto.AppDto;
import com.staircase13.apperta.service.dto.CompositionParametersDto;
import com.staircase13.apperta.service.dto.QueryTemplateDto;
import com.staircase13.apperta.service.exception.InvalidAppNameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AppService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AppService.class);

    private final DeviceAppRepository appRepository;

    private final TemplateRepository templateRepository;

    private final CompositionParametersRepository parametersRepository;

    private final QueryTemplateRespository queryTemplateRespository;

    @Autowired
    public AppService(DeviceAppRepository appRepository, TemplateRepository templateRepository,
                      CompositionParametersRepository compositionParametersRepository, QueryTemplateRespository queryTemplateRespository) {
        this.appRepository = appRepository;
        this.templateRepository = templateRepository;
        this.parametersRepository = compositionParametersRepository;
        this.queryTemplateRespository = queryTemplateRespository;
    }

    public boolean checkAppExists(String appName) {
        Optional<DeviceApp> app = appRepository.findByAppName(appName);
        return app.isPresent();
    }

    @Transactional
    public void registerApp(AppDto appDto) {
        DeviceApp app = DeviceApp.builder().appName(appDto.getAppName()).build();
        appRepository.saveAndFlush(app);

        if (appDto.getRequiredEhrTemplates() != null && appDto.getRequiredEhrTemplates().size() > 0) {
            updateTemplates(app, appDto.getRequiredEhrTemplates());
        }

        if (appDto.getCompositionParameters() != null) {
            updateParameters(app, appDto.getCompositionParameters());
        }

        if (appDto.getQueryTemplates() != null) {
            updateQueryTemplates(app, appDto.getQueryTemplates());
        }

        // TODO: handle not valid exceptions?
    }

    @Transactional
    public void deleteApp(String appName) {
        Optional<DeviceApp> appOptional = appRepository.findByAppName(appName);
        if (appOptional.isPresent()) {
            appRepository.delete(appOptional.get());
        }
    }

    @Transactional
    public List<String> getTemplatesForApp(String appName) throws InvalidAppNameException {
        Optional<DeviceApp> appOptional = appRepository.findByAppName(appName);
        appOptional.orElseThrow(() -> new InvalidAppNameException(appName));
        return templateRepository.findByApp(appOptional.get())
                .stream().map(t -> t.getTemplate()).collect(Collectors.toList());
    }

    @Transactional
    public List<QueryTemplateDto> getQueryTemplatesForApp(String appName) throws InvalidAppNameException {
        Optional<DeviceApp> appOptional = appRepository.findByAppName(appName);
        appOptional.orElseThrow(() -> new InvalidAppNameException(appName));
        return queryTemplateRespository.findByApp(appOptional.get())
                .stream().map(q -> mapQueryTemplateToDto(q)).collect(Collectors.toList());
    }

    @Transactional
    public List<CompositionParametersDto> getCompositionParametersForApp(String appName) throws InvalidAppNameException {
        Optional<DeviceApp> appOptional = appRepository.findByAppName(appName);
        appOptional.orElseThrow(() -> new InvalidAppNameException(appName));
        return parametersRepository.findByApp(appOptional.get())
                .stream().map(p -> mapCompositionParametersToDto(p)).collect(Collectors.toList());
    }

    @Transactional
    public List<CompositionParametersDto> getCompositionParametersForAppAndSet(String appName, String paramSet) throws InvalidAppNameException {
        Optional<DeviceApp> appOptional = appRepository.findByAppName(appName);
        appOptional.orElseThrow(() -> new InvalidAppNameException(appName));
        return parametersRepository.findByAppAndSetName(appOptional.get(), paramSet)
                .stream().map(p -> mapCompositionParametersToDto(p)).collect(Collectors.toList());
    }

    public List<Template> updateTemplates(DeviceApp app, List<String> requiredTemplates) {

        templateRepository.deleteByApp(app);

        List<Template> templates = requiredTemplates.stream()
                        .map(tname -> Template.builder().app(app).template(tname).build()).collect(Collectors.toList());
        templateRepository.saveAll(templates);
        return templates;
    }

    public List<CompositionParameters> updateParameters(DeviceApp app, List<CompositionParametersDto> parametersDtos) {

        parametersRepository.deleteByApp(app);

        List<CompositionParameters> parameters = parametersDtos.stream()
                .map(params -> mapDtoToCompositionParameters(app, params))
                .collect(Collectors.toList());
        parametersRepository.saveAll(parameters);
        return parameters;
    }

    @Transactional
    public List<QueryTemplate> updateQueryTemplates(DeviceApp app, List<QueryTemplateDto> queryTemplateDtos) {

        queryTemplateRespository.deleteByApp(app);

        List<QueryTemplate> queryTemplates = queryTemplateDtos.stream()
                .map(query -> mapDtoToQueryTemplate(app, query))
                .collect(Collectors.toList());
        queryTemplateRespository.saveAll(queryTemplates);
        return queryTemplates;
    }

    private QueryTemplate mapDtoToQueryTemplate(DeviceApp app, QueryTemplateDto dto) {
        return QueryTemplate.builder().app(app).name(dto.getName()).template(dto.getTemplate()).build();
    }

    private QueryTemplateDto mapQueryTemplateToDto(QueryTemplate queryTemplate) {
        return QueryTemplateDto.builder().name(queryTemplate.getName()).template(queryTemplate.getTemplate()).build();
    }

    private CompositionParameters mapDtoToCompositionParameters(DeviceApp app, CompositionParametersDto dto) {
        return CompositionParameters.builder().app(app).setName(dto.getSetName()).
                parameterName(dto.getParameterName()).ehrName(dto.getEhrName()).build();
    }

    private CompositionParametersDto mapCompositionParametersToDto(CompositionParameters parameters) {
        return CompositionParametersDto.builder()
                .setName(parameters.getSetName())
                .parameterName(parameters.getParameterName())
                .ehrName(parameters.getEhrName())
                .build();
    }
}
