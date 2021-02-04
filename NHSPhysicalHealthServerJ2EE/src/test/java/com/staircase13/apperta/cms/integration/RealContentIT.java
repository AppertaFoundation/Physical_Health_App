package com.staircase13.apperta.cms.integration;

import com.google.common.collect.Sets;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.entities.CmsPageFragment;
import com.staircase13.apperta.cms.entities.CmsPageLink;
import com.staircase13.apperta.cms.repository.CmsPageRepository;
import com.staircase13.apperta.cms.tar.CmsContentAggregator;
import com.staircase13.apperta.integration.AbstractIntegrationTest;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashSet;
import java.util.Set;

import static com.staircase13.apperta.cms.entities.NhsApi.CONDITIONS;
import static com.staircase13.apperta.cms.entities.NhsApi.LIVE_WELL;
import static java.util.Arrays.asList;

/**
 * This test can be used to end to end test CMS TAR generation by configuring application.properties to use the actual URL and token.
 * Otherwise, it should be ignored
 */
@Ignore
public class RealContentIT extends AbstractIntegrationTest {

    @Autowired
    private CmsContentAggregator cmsContentAggregator;

    @Autowired
    private CmsPageRepository cmsPageRepository;

    @Test
    public void buildExampleTar() {
        setupCmsConfig();
        cmsContentAggregator.aggregateToTar();
    }

    private void setupCmsConfig() {
        addPageConfiguration("acne",
                "extra information",
                newLinkedHashSet(
                CmsPageFragment.builder()
                        .api(CONDITIONS)
                        .primaryEntityName("acne")
                        .jsonSectionHeading("Types of spots")
                        .jsonMainEntityPosition(0)
                        .cmsPageOrder(0)
                        .build(),
                CmsPageFragment.builder()
                        .api(CONDITIONS)
                        .primaryEntityName("acne")
                        .jsonSectionHeading("Who's affected?")
                        .jsonMainEntityPosition(0)
                        .cmsPageOrder(1)
                        .build(),
                CmsPageFragment.builder()
                        .api(LIVE_WELL)
                        .primaryEntityName("eat-well")
                        .cmsPageOrder(1)
                        .jsonSectionHeading("Food groups in your diet")
                        .jsonMainEntityPosition(0)
                        .cmsPageOrder(2)
                        .build()
                ),
                newLinkedHashSet(
                    CmsPageLink.builder()
                            .url("https://www.nhs.uk/conditions/acne/")
                            .label("More Information")
                            .description("The full information page for Acne")
                            .build()
                )
        );

        addPageConfiguration("kidneys-misc",
                "more information",
                newLinkedHashSet(
                    CmsPageFragment.builder()
                            .api(CONDITIONS)
                            .primaryEntityName("prostate-enlargement")
                            .secondaryEntityName("diagnosis")
                            .cmsPageOrder(0)
                            .jsonSectionHeading("GP examination and tests")
                            .jsonMainEntityPosition(0)
                            .cmsPageOrder(0)
                            .build(),
                    CmsPageFragment.builder()
                            .api(CONDITIONS)
                            .primaryEntityName("autosomal-dominant-polycystic-kidney-disease-adpkd")
                            .jsonSectionHeading("What causes ADPKD")
                            .jsonMainEntityPosition(0)
                            .cmsPageOrder(1)
                            .build(),
                    CmsPageFragment.builder()
                            .api(LIVE_WELL)
                            .primaryEntityName("eat-well")
                            .secondaryEntityName("eat-less-saturated-fat")
                            .jsonSectionHeading("How to cut down on saturated fat")
                            .cmsPageOrder(2)
                            .jsonMainEntityPosition(0).build()
                ),
                newLinkedHashSet()
        );
    }

    private void addPageConfiguration(String pageName, String linksHeader, Set<CmsPageFragment> fragments, Set<CmsPageLink> links) {
        CmsPage page = cmsPageRepository.save(CmsPage.builder().name(pageName).linksHeader(linksHeader).build());
        fragments.stream().forEach(f -> f.setCmsPage(page));
        links.stream().forEach(f -> f.setCmsPage(page));
        page.setFragments(fragments);
        page.setLinks(links);
        cmsPageRepository.save(page);
        cmsPageRepository.flush();
    }

    private <T> LinkedHashSet<T> newLinkedHashSet(T... elements) {
        return Sets.newLinkedHashSet(asList(elements));
    }
}
