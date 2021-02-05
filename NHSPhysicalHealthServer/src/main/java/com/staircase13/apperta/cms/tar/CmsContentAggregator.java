package com.staircase13.apperta.cms.tar;

import com.staircase13.apperta.cms.LoadLog;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.repository.CmsPageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.reverseOrder;

@Service
public class CmsContentAggregator {
    private final CmsTarBuilderFactory cmsTarBuilderFactory;

    private final CmsPageRepository cmsPageRepository;

    private final Clock clock;

    private final CmsContentFileBuilder cmsContentFileBuilder;

    @Autowired
    public CmsContentAggregator(CmsTarBuilderFactory cmsTarBuilderFactory, CmsPageRepository cmsPageRepository, Clock clock, CmsContentFileBuilder cmsContentFileBuilder) {
        this.cmsTarBuilderFactory = cmsTarBuilderFactory;
        this.cmsPageRepository = cmsPageRepository;
        this.clock = clock;
        this.cmsContentFileBuilder = cmsContentFileBuilder;
    }

    @Transactional
    public LoadLog aggregateToTar() {
        return aggregateToTar(new LoadLog());
    }

    public LoadLog aggregateToTar(LoadLog loadLog) {
        try {
            CmsTarBuilder tarBuilder = cmsTarBuilderFactory.newTarBuilder();

            List<CmsPage> cmsPages = cmsPageRepository.findAll();

            aggregateToTar(loadLog, tarBuilder, cmsPages);
        } catch(IOException e) {
            loadLog.addErrorLog("Unexpected Error '" + e.getMessage() + "'");
        }

        return loadLog;
    }

    public Optional<Path> aggregateToTar(LoadLog loadLog, CmsTarBuilder tarBuilder, List<CmsPage> cmsPages) throws IOException {
        if(cmsPages.isEmpty()) {
            loadLog.addErrorLog("No CMS Pages Configured. Will not build TAR");
            tarBuilder.rollback();
            return Optional.empty();
        }

        Set<LocalDateTime> contentModifieds = new HashSet<>();
        for(CmsPage cmsPage : cmsPages) {

            try {
                loadLog.addInfoLog("Adding page for '%s'",cmsPage.getName());

                LocalDateTime modified = cmsContentFileBuilder.addPage(tarBuilder, cmsPage, loadLog);
                contentModifieds.add(modified);

                loadLog.addInfoLog("Added page '%s'",cmsPage.getName());

            } catch(AggregationException e) {
                loadLog.addErrorLog("Discarding page '%s' as it could not be created with reason '%s'",cmsPage.getName(),e.getMessage());
            }

        }

        /*
         If we haven't loaded any pages, we'll produce an empty TAR with a last updated
         date of 'now'. This forces devices to re-download, but given it's a tiny TAR, this
         shouldn't be an issue and ensures any cached content they have is removed
         */
        LocalDateTime newestContentModified = contentModifieds.stream().sorted(reverseOrder()).findFirst().orElse(LocalDateTime.now(clock));

        Optional<Path> tar = tarBuilder.commit(newestContentModified);

        if(tar.isPresent()) {
            loadLog.addInfoLog("Written TAR to '%s' with content modified date '%s'", tar.get().toAbsolutePath(), newestContentModified);
        }

        return tar;
    }
}
