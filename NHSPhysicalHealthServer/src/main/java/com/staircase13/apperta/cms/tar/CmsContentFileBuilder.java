package com.staircase13.apperta.cms.tar;

import com.staircase13.apperta.cms.LoadLog;
import com.staircase13.apperta.cms.NhsApiContent;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.entities.CmsPageFragment;
import com.staircase13.apperta.cms.entities.CmsPageLink;
import com.staircase13.apperta.cms.loader.CmsContentLoader;
import com.staircase13.apperta.cms.loader.NhsContentKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Comparator.reverseOrder;

@Service
public class CmsContentFileBuilder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CmsContentFileBuilder.class);

    private final CmsContentLoader cmsContentLoader;

    @Autowired
    public CmsContentFileBuilder(CmsContentLoader cmsContentLoader) {
        this.cmsContentLoader = cmsContentLoader;
    }

    public LocalDateTime addPage(CmsTarBuilder tarBuilder, CmsPage cmsPage, LoadLog loadLog) throws AggregationException {

        StringBuilder contentBuilder = new StringBuilder();

        contentBuilder.append("<html><body>\n");
        Set<LocalDateTime> contentModifieds = generateHtml(cmsPage, loadLog, contentBuilder);
        contentBuilder.append("</body></html>");

        try {
            tarBuilder.addPage(
                    String.format("%s.html", cmsPage.getName()),
                    contentBuilder.toString());
        } catch(IOException e) {
            throw new AggregationException("Cannot write fragment to TAR",e);
        }

        // if the page doesn't include any actual NHS content (i.e. just links)
        // we set the content modified date to whenever the page config was
        // last updated
        if(contentModifieds.isEmpty()) {
            return cmsPage.getModified();
        }

        LocalDateTime latestContentModified = contentModifieds.stream().sorted(reverseOrder()).findFirst().get();
        // if the page config was updated after the content was last modified, we
        // use the date the config was changed
        return latestContentModified.isAfter(cmsPage.getModified()) ? latestContentModified : cmsPage.getModified();
    }

    public Set<LocalDateTime> generateHtml(CmsPage cmsPage, LoadLog loadLog, StringBuilder html) throws AggregationException {

        Map<NhsContentKey, NhsApiContent> apiContentCache = collateContent(cmsPage, loadLog);

        addTrackers(html, apiContentCache);
        addFragments(cmsPage, html, apiContentCache);
        addLinks(cmsPage, loadLog, html);

        return apiContentCache.values().stream().map(apiContent -> apiContent.getLastModified()).collect(Collectors.toSet());
    }

    private Map<NhsContentKey, NhsApiContent> collateContent(CmsPage cmsPage,
                                                             LoadLog loadLog) throws AggregationException {
        Map<NhsContentKey, NhsApiContent> content = new LinkedHashMap<>();
        for(CmsPageFragment fragment : cmsPage.getFragments()) {

            NhsContentKey key = NhsContentKey.builder()
                    .nhsApi(fragment.getApi())
                    .primaryEntityName(fragment.getPrimaryEntityName())
                    .secondaryEntityName(fragment.getSecondaryEntityName())
                    .build();

            LOGGER.debug("Loading API Fragment for '{}'",key);

            Optional<NhsApiContent> apiContent = cmsContentLoader.getApiContent(key,loadLog);
            if(!apiContent.isPresent()) {
                throw new AggregationException("Cannot find content");
            }

            content.put(key,apiContent.get());

        }
        return content;
    }

    private void addTrackers(StringBuilder html, Map<NhsContentKey, NhsApiContent> apiContentCache) {
        if(apiContentCache.isEmpty()) {
            return;
        }

        html.append("<div class=\"cms-trackers\">\n");
        apiContentCache.values().forEach(content ->
                content.getTrackerHtml().forEach(trackerHtml -> {
                    html.append(trackerHtml);
                    html.append("\n");
                })
        );
        html.append("</div>\n");
    }

    private void addFragments(CmsPage cmsPage, StringBuilder html, Map<NhsContentKey, NhsApiContent> apiContentCache) throws AggregationException {
        for(CmsPageFragment fragment : cmsPage.getFragments()) {
            addFragment(html, fragment, apiContentCache);
        }
    }

    private void addFragment(StringBuilder html,
                                      CmsPageFragment fragment,
                                      Map<NhsContentKey, NhsApiContent> apiContentCache) throws AggregationException {

        NhsContentKey key = NhsContentKey.builder()
                .nhsApi(fragment.getApi())
                .primaryEntityName(fragment.getPrimaryEntityName())
                .secondaryEntityName(fragment.getSecondaryEntityName())
                .build();

        NhsApiContent apiContent = apiContentCache.get(key);

        Optional<String> fragmentContent = apiContent.getFragment(
                NhsApiContent.ContentCoordinates.builder()
                        .sectionHeading(fragment.getJsonSectionHeading())
                        .mainEntityPosition(fragment.getJsonMainEntityPosition())
                        .build());

        if(!fragmentContent.isPresent()) {
            throw new AggregationException("Loaded page but cannot find fragment " + fragment);
        }

        html.append(String.format("<div class=\"cms-fragment\">%s</div>\n",fragmentContent.get()));
    }

    private void addLinks(CmsPage cmsPage, LoadLog loadLog, StringBuilder html) {
        if(cmsPage.getLinks().isEmpty()) {
            return;
        }

        html.append(String.format("<h2>%s</h2>\n",cmsPage.getLinksHeader()));
        html.append("<dl class=\"cms-links\">\n");
        for(CmsPageLink link : cmsPage.getLinks()) {
            addLink(html, link, loadLog);
        }
        html.append("</dl>\n");
    }

    private void addLink(StringBuilder content, CmsPageLink link, LoadLog loadLog) {

        loadLog.addInfoLog("Adding link to '%s'",link.getUrl());

        content.append(String.format("<dt><a href=\"%s\">%s</a></dt>\n",link.getUrl(),link.getLabel()));
        content.append(String.format("<dd>%s</dd>\n",link.getDescription()));
    }
}
