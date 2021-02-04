package com.staircase13.apperta.cms;

import com.google.common.collect.Sets;
import com.staircase13.apperta.cms.dto.CmsPageDto;
import com.staircase13.apperta.cms.dto.CmsPageFragmentDto;
import com.staircase13.apperta.cms.dto.CmsPageLinkDto;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.entities.CmsPageFragment;
import com.staircase13.apperta.cms.entities.CmsPageLink;
import com.staircase13.apperta.cms.entities.NhsApi;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public class CmsDtoAdapterTest {

    private static final String PAGE_NAME = "pageName";
    private static final LocalDateTime PAGE_MODIFIED = LocalDateTime.now();

    private static final NhsApi FRAGMENT_1_API = NhsApi.CONDITIONS;
    private static final int FRAGMENT_1_PAGE_ORDER = 1;
    private static final String FRAGMENT_1_PRIMARY_ENTITY = "f1primary";
    private static final String FRAGMENT_1_SECONDARY_ENTITY = "f1secondary";
    private static final String FRAGMENT_1_SECTION_HEADING = "f1sectionHeading";
    private static final int FRAGMENT_1_ENTITY_POS = 2;

    private static final NhsApi FRAGMENT_2_API = NhsApi.CONDITIONS;
    private static final int FRAGMENT_2_PAGE_ORDER = 1;
    private static final String FRAGMENT_2_PRIMARY_ENTITY = "f1primary";
    private static final String FRAGMENT_2_SECONDARY_ENTITY = "f1secondary";
    private static final String FRAGMENT_2_SECTION_HEADING = "f1sectionHeading";
    private static final int FRAGMENT_2_ENTITY_POS = 2;
    
    private static final String LINK_1_URL = "http://url";
    private static final String LINK_1_LABEL = "label";
    private static final String LINK_1_DESCRIPTION = "description";

    private static final String LINK_2_URL = "http://url";
    private static final String LINK_2_LABEL = "label";
    private static final String LINK_2_DESCRIPTION = "description";

    private static final String LINKS_HEADER = "theLinksHeader";

    @Test
    public void to_dto() {
        CmsPage page = CmsPage.builder()
                .name(PAGE_NAME)
                .modified(PAGE_MODIFIED)
                .linksHeader(LINKS_HEADER)
                .fragments(Sets.newHashSet(
                        CmsPageFragment.builder()
                                .api(FRAGMENT_1_API)
                                .cmsPageOrder(FRAGMENT_1_PAGE_ORDER)
                                .primaryEntityName(FRAGMENT_1_PRIMARY_ENTITY)
                                .secondaryEntityName(FRAGMENT_1_SECONDARY_ENTITY)
                                .jsonSectionHeading(FRAGMENT_1_SECTION_HEADING)
                                .jsonMainEntityPosition(FRAGMENT_1_ENTITY_POS)
                                .build(),
                        CmsPageFragment.builder()
                                .api(FRAGMENT_2_API)
                                .cmsPageOrder(FRAGMENT_2_PAGE_ORDER)
                                .primaryEntityName(FRAGMENT_2_PRIMARY_ENTITY)
                                .secondaryEntityName(FRAGMENT_2_SECONDARY_ENTITY)
                                .jsonSectionHeading(FRAGMENT_2_SECTION_HEADING)
                                .jsonMainEntityPosition(FRAGMENT_2_ENTITY_POS)
                                .build()
                ))
                .links(Sets.newHashSet(
                        CmsPageLink.builder()
                                .url(LINK_1_URL)
                                .label(LINK_1_LABEL)
                                .description(LINK_1_DESCRIPTION)
                                .build(),
                        CmsPageLink.builder()
                                .url(LINK_2_URL)
                                .label(LINK_2_LABEL)
                                .description(LINK_2_DESCRIPTION)
                                .build()
                ))
                .build();

        CmsPageDto dto = new CmsDtoAdapter().toDto(page);

        assertThat(dto.getName(), is(PAGE_NAME));
        assertThat(dto.getModified(), is(PAGE_MODIFIED));
        assertThat(dto.getLinksHeader(), is(LINKS_HEADER));

        assertThat(dto.getFragments(), hasSize(2));
        assertThat(dto.getFragments(), hasItem(
                allOf(
                        hasProperty("api",is(FRAGMENT_1_API.name())),
                        hasProperty("primaryEntityName",is(FRAGMENT_1_PRIMARY_ENTITY)),
                        hasProperty("secondaryEntityName",is(FRAGMENT_1_SECONDARY_ENTITY)),
                        hasProperty("jsonSectionHeading",is(FRAGMENT_1_SECTION_HEADING)),
                        hasProperty("jsonMainEntityPosition",is(FRAGMENT_1_ENTITY_POS))
                )
        ));
        assertThat(dto.getFragments(), hasItem(
                allOf(
                        hasProperty("api",is(FRAGMENT_2_API.name())),
                        hasProperty("primaryEntityName",is(FRAGMENT_2_PRIMARY_ENTITY)),
                        hasProperty("secondaryEntityName",is(FRAGMENT_2_SECONDARY_ENTITY)),
                        hasProperty("jsonSectionHeading",is(FRAGMENT_2_SECTION_HEADING)),
                        hasProperty("jsonMainEntityPosition",is(FRAGMENT_2_ENTITY_POS))
                )
        ));

        assertThat(dto.getLinks(), hasSize(2));
        assertThat(dto.getLinks(), hasItem(
                allOf(
                        hasProperty("url",is(LINK_1_URL)),
                        hasProperty("label",is(LINK_1_LABEL)),
                        hasProperty("description",is(LINK_1_DESCRIPTION))
                )
        ));
        assertThat(dto.getLinks(), hasItem(
                allOf(
                        hasProperty("url",is(LINK_2_URL)),
                        hasProperty("label",is(LINK_2_LABEL)),
                        hasProperty("description",is(LINK_2_DESCRIPTION))
                )
        ));
    }

    @Test
    public void to_page() {
        CmsPageDto pageDto = CmsPageDto.builder()
                .name(PAGE_NAME)
                .modified(PAGE_MODIFIED)
                .linksHeader(LINKS_HEADER)
                .fragments(Arrays.asList(
                        CmsPageFragmentDto.builder()
                                .api(FRAGMENT_1_API.name())
                                .primaryEntityName(FRAGMENT_1_PRIMARY_ENTITY)
                                .secondaryEntityName(FRAGMENT_1_SECONDARY_ENTITY)
                                .jsonSectionHeading(FRAGMENT_1_SECTION_HEADING)
                                .jsonMainEntityPosition(FRAGMENT_1_ENTITY_POS)
                                .build(),
                        CmsPageFragmentDto.builder()
                                .api(FRAGMENT_2_API.name())
                                .primaryEntityName(FRAGMENT_2_PRIMARY_ENTITY)
                                .secondaryEntityName(FRAGMENT_2_SECONDARY_ENTITY)
                                .jsonSectionHeading(FRAGMENT_2_SECTION_HEADING)
                                .jsonMainEntityPosition(FRAGMENT_2_ENTITY_POS)
                                .build()
                ))
                .links(Arrays.asList(
                        CmsPageLinkDto.builder()
                                .url(LINK_1_URL)
                                .label(LINK_1_LABEL)
                                .description(LINK_1_DESCRIPTION)
                                .build(),
                        CmsPageLinkDto.builder()
                                .url(LINK_2_URL)
                                .label(LINK_2_LABEL)
                                .description(LINK_2_DESCRIPTION)
                                .build()
                ))
                .build();

        CmsPage page = new CmsDtoAdapter().toPage(pageDto);

        assertThat(page.getName(), is(PAGE_NAME));
        assertThat(page.getModified(), is(PAGE_MODIFIED));
        assertThat(page.getLinksHeader(), is(LINKS_HEADER));

        assertThat(page.getFragments(), hasSize(2));
        assertThat(page.getFragments(), hasItem(
                allOf(
                        hasProperty("api",is(FRAGMENT_1_API)),
                        hasProperty("primaryEntityName",is(FRAGMENT_1_PRIMARY_ENTITY)),
                        hasProperty("secondaryEntityName",is(FRAGMENT_1_SECONDARY_ENTITY)),
                        hasProperty("jsonSectionHeading",is(FRAGMENT_1_SECTION_HEADING)),
                        hasProperty("jsonMainEntityPosition",is(FRAGMENT_1_ENTITY_POS)),
                        hasProperty("cmsPageOrder",is(0))
                )
        ));
        assertThat(page.getFragments(), hasItem(
                allOf(
                        hasProperty("api",is(FRAGMENT_2_API)),
                        hasProperty("primaryEntityName",is(FRAGMENT_2_PRIMARY_ENTITY)),
                        hasProperty("secondaryEntityName",is(FRAGMENT_2_SECONDARY_ENTITY)),
                        hasProperty("jsonSectionHeading",is(FRAGMENT_2_SECTION_HEADING)),
                        hasProperty("jsonMainEntityPosition",is(FRAGMENT_2_ENTITY_POS)),
                        hasProperty("cmsPageOrder",is(1))
                )
        ));

        assertThat(page.getLinks(), hasSize(2));
        assertThat(page.getLinks(), hasItem(
                allOf(
                        hasProperty("url",is(LINK_1_URL)),
                        hasProperty("label",is(LINK_1_LABEL)),
                        hasProperty("description",is(LINK_1_DESCRIPTION)),
                        hasProperty("cmsPageOrder",is(0))
                )
        ));
        assertThat(page.getLinks(), hasItem(
                allOf(
                        hasProperty("url",is(LINK_2_URL)),
                        hasProperty("label",is(LINK_2_LABEL)),
                        hasProperty("description",is(LINK_2_DESCRIPTION)),
                        hasProperty("cmsPageOrder",is(1))
                )
        ));

    }

}
