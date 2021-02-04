package com.staircase13.apperta.cms;

import com.staircase13.apperta.cms.dto.CmsPageDto;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.repository.CmsCacheRepository;
import com.staircase13.apperta.cms.repository.CmsPageRepository;
import com.staircase13.apperta.cms.tar.AggregationException;
import com.staircase13.apperta.cms.tar.CmsContentAggregator;
import com.staircase13.apperta.cms.tar.CmsContentFileBuilder;
import com.staircase13.apperta.cms.tar.CmsTarBuilderFactory;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CmsService {
    private static final Logger LOGGER = LoggerFactory.getLogger(Logger.class);

    private final CmsPageRepository cmsPageRepository;

    private final CmsDtoAdapter cmsDtoAdapter;

    private final CmsCacheRepository cmsCacheRepository;

    private final CmsContentAggregator cmsContentAggregator;

    private final CmsTarBuilderFactory cmsTarBuilderFactory;

    private final CmsContentFileBuilder cmsContentFileBuilder;

    @Autowired
    public CmsService(CmsPageRepository cmsPageRepository, CmsDtoAdapter cmsDtoAdapter, CmsCacheRepository cmsCacheRepository, CmsContentAggregator cmsContentAggregator, CmsTarBuilderFactory cmsTarBuilderFactory, CmsContentFileBuilder cmsContentFileBuilder) {
        this.cmsPageRepository = cmsPageRepository;
        this.cmsDtoAdapter = cmsDtoAdapter;
        this.cmsCacheRepository = cmsCacheRepository;
        this.cmsContentAggregator = cmsContentAggregator;
        this.cmsTarBuilderFactory = cmsTarBuilderFactory;
        this.cmsContentFileBuilder = cmsContentFileBuilder;
    }

    public List<CmsPageDto> getPages() {
        return cmsPageRepository.findAll()
                .stream()
                .map(p -> cmsDtoAdapter.toDto(p))
                .collect(Collectors.toList());
    }

    public LoadLog saveChanges(List<CmsPageDto> cmsPageDtos) {
        LOGGER.info("Saving changes to CMS Pages");
        cmsPageRepository.deleteAll();
        cmsPageRepository.flush();

        for(CmsPage page : toPages(cmsPageDtos)) {
            LOGGER.info("Saving page '{}', last modified '{}'",page.getName(),page.getModified());
            cmsPageRepository.save(page);
            cmsPageRepository.flush();
        }
        LOGGER.info("All CMS Page changes saved. Regenerating TAR");

        return cmsContentAggregator.aggregateToTar();
    }

    public LoadLog flushCache()  {
        LOGGER.info("Flushing the cache");
        cmsCacheRepository.deleteAll();
        cmsCacheRepository.flush();

        return cmsContentAggregator.aggregateToTar();
    }

    public LoadLog getLoadLog(List<CmsPageDto> cmsPageDtos) throws IOException {
        LoadLog loadLog = new LoadLog();
        cmsContentAggregator.aggregateToTar(loadLog,cmsTarBuilderFactory.noOpTarBuilder(),toPages(cmsPageDtos));
        return loadLog;
    }

    public TemporaryTarResponse getTemporaryTar(List<CmsPageDto> cmsPageDtos) throws IOException {
        LoadLog loadLog = new LoadLog();
        Optional<Path> tar = cmsContentAggregator.aggregateToTar(loadLog,cmsTarBuilderFactory.newTemporaryTarBuilder(),toPages(cmsPageDtos));
        return new TemporaryTarResponse(tar,loadLog);
    }

    public String previewHtml(CmsPageDto page, LoadLog loadLog) {
        try {
            loadLog.addInfoLog("Generating preview for page '%s'",page.getName());
            StringBuilder content = new StringBuilder();
            cmsContentFileBuilder.generateHtml(cmsDtoAdapter.toPage(page), loadLog, content);
            return content.toString();
        } catch(AggregationException e) {
            loadLog.addErrorLog("Error building page '%s'",e.getMessage());
            return "";
        }
    }

    public void rebuildTar() {
        LOGGER.info("Rebuilding CMS Tar");
        cmsContentAggregator.aggregateToTar();
        LOGGER.info("CMS Tar Rebuild Complete");
    }

    private List<CmsPage> toPages(List<CmsPageDto> pageDtos) {
        return pageDtos.stream().map(p -> cmsDtoAdapter.toPage(p)).collect(Collectors.toList());
    }

    @Getter
    @AllArgsConstructor
    public class TemporaryTarResponse {
        private final Optional<Path> tar;
        private final LoadLog loadLog;
    }
}
