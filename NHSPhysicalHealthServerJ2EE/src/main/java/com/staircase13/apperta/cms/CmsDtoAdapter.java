package com.staircase13.apperta.cms;

import com.staircase13.apperta.cms.dto.CmsPageDto;
import com.staircase13.apperta.cms.dto.CmsPageFragmentDto;
import com.staircase13.apperta.cms.dto.CmsPageLinkDto;
import com.staircase13.apperta.cms.entities.CmsPage;
import com.staircase13.apperta.cms.entities.CmsPageFragment;
import com.staircase13.apperta.cms.entities.CmsPageLink;
import com.staircase13.apperta.cms.entities.NhsApi;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Service
public class CmsDtoAdapter {

    public CmsPageDto toDto(CmsPage page) {
        return CmsPageDto.builder()
                .name(page.getName())
                .modified(page.getModified())
                .linksHeader(page.getLinksHeader())
                .fragments(page.getFragments().stream().map(f -> toDto(f)).collect(toList()))
                .links(page.getLinks().stream().map(f -> toDto(f)).collect(toList()))
                .build();
    }

    private CmsPageFragmentDto toDto(CmsPageFragment fragment) {
        return CmsPageFragmentDto.builder()
                .api(fragment.getApi().name())
                .primaryEntityName(fragment.getPrimaryEntityName())
                .secondaryEntityName(fragment.getSecondaryEntityName())
                .jsonSectionHeading(fragment.getJsonSectionHeading())
                .jsonMainEntityPosition(fragment.getJsonMainEntityPosition())
                .build();
    }

    private CmsPageLinkDto toDto(CmsPageLink link) {
        return CmsPageLinkDto.builder()
                .url(link.getUrl())
                .label(link.getLabel())
                .description(link.getDescription())
                .build();
    }

    public CmsPage toPage(CmsPageDto dto) {
        CmsPage cmsPage = CmsPage.builder()
                .name(dto.getName())
                .modified(dto.getModified())
                .linksHeader(dto.getLinksHeader())
                .build();

        cmsPage.setFragments(
                        IntStream.range(0,dto.getFragments().size())
                                .mapToObj(i -> toFragment(cmsPage,dto.getFragments().get(i),i))
                                .collect(Collectors.toCollection(LinkedHashSet::new))
                );

        cmsPage.setLinks(
                        IntStream.range(0,dto.getLinks().size())
                                .mapToObj(i -> toLink(cmsPage,dto.getLinks().get(i),i))
                                .collect(Collectors.toCollection(LinkedHashSet::new))
                );

        return cmsPage;
    }

    private CmsPageFragment toFragment(CmsPage cmsPage, CmsPageFragmentDto fragment, int index) {
        return CmsPageFragment.builder()
                .cmsPage(cmsPage)
                .cmsPageOrder(index)
                .api(NhsApi.valueOf(fragment.getApi()))
                .primaryEntityName(fragment.getPrimaryEntityName())
                .secondaryEntityName(fragment.getSecondaryEntityName())
                .jsonSectionHeading(fragment.getJsonSectionHeading())
                .jsonMainEntityPosition(fragment.getJsonMainEntityPosition())
                .build();
    }

    private CmsPageLink toLink(CmsPage cmsPage, CmsPageLinkDto link, int index) {
        return CmsPageLink.builder()
                .cmsPage(cmsPage)
                .cmsPageOrder(index)
                .url(link.getUrl())
                .label(link.getLabel())
                .description(link.getDescription())
                .build();
    }

}
