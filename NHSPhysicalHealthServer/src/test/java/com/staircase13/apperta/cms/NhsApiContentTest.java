package com.staircase13.apperta.cms;

import com.jayway.jsonpath.InvalidJsonException;
import com.staircase13.apperta.cms.entities.NhsApi;
import com.staircase13.apperta.cms.loader.NhsContentKey;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.LocalDateTime;
import java.util.List;

import static com.staircase13.apperta.cms.CmsTestData.*;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

public class NhsApiContentTest {

    private static final String URL_PREFIX = "http://test.prefix";

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    private NhsContentKey createKey(String primaryName) {
        return NhsContentKey.builder().nhsApi(NhsApi.CONDITIONS).primaryEntityName(primaryName).build();
    }

    @Test
    public void invalid_json_throw_exception() throws Exception {
        exceptionRule.expectMessage("Could not load API Content 'NhsContentKey(nhsApi=CONDITIONS, primaryEntityName=invalid-content, secondaryEntityName=null)'");
        exceptionRule.expect(CannotParseApiContentException.class);
        exceptionRule.expectCause(instanceOf(InvalidJsonException.class));

        new NhsApiContent(createKey("invalid-content"), getInvalidConditionResponse(), URL_PREFIX);
    }

    @Test
    public void get_last_modified() throws Exception {

        LocalDateTime modified = new NhsApiContent(createKey("valid-content"), getValidConditionResponse(), URL_PREFIX).getLastModified();
        assertThat(modified, is(LocalDateTime.of(2019,1,18,15,52,07,352908000)));
    }

    @Test
    public void get_main_entity_no_child_main_entities() throws Exception {
        NhsApiContent nhsApiContent = new NhsApiContent(createKey("valid-content"), getValidConditionResponse(), URL_PREFIX);

        String fragment = nhsApiContent.getFragment(NhsApiContent.
                ContentCoordinates.builder()
                .sectionHeading("One Main Entity, No Children")
                .mainEntityPosition(0)
                .build()).get();

        assertThat(fragment,
                is("<p>One Main Entity, No Child Content. <strong>Position 0</strong></p>"));
    }

    @Test
    public void get_main_entity_no_child_main_entities_different_position() throws Exception {
        NhsApiContent nhsApiContent = new NhsApiContent(createKey("valid-content"), getValidConditionResponse(), URL_PREFIX);

        String fragment = nhsApiContent.getFragment(NhsApiContent.
                ContentCoordinates.builder()
                .sectionHeading("One Main Entity, No Children")
                .mainEntityPosition(1)
                .build()).get();

        assertThat(fragment,
                is("<p>One Main Entity, No Child Content. <strong>Position 1</strong></p>"));
    }

    @Test
    public void get_main_entity_has_child_main_entity() throws Exception {
        NhsApiContent nhsApiContent = new NhsApiContent(createKey("valid-content"), getValidConditionResponse(), URL_PREFIX);

        String fragment = nhsApiContent.getFragment(NhsApiContent.
                ContentCoordinates.builder()
                .sectionHeading("Main Entity with Child Entity")
                .mainEntityPosition(1)
                .build()).get();

        assertThat(fragment,
                is("<p>Child Entity of Main Entity</p>"));
    }

    @Test
    public void get_main_entity_single_quote_in_section_heading() throws Exception {
        NhsApiContent nhsApiContent = new NhsApiContent(createKey("valid-content"), getValidConditionResponse(), URL_PREFIX);

        String fragment = nhsApiContent.getFragment(NhsApiContent.
                ContentCoordinates.builder()
                .sectionHeading("Section heading with 'single quotes'")
                .mainEntityPosition(0)
                .build()).get();

        assertThat(fragment,
                is("<p>Main entity for section heading with single quotes</p>"));
    }

    @Test
    public void get_main_entity_section_heading_doesnt_exist() throws Exception {
        NhsApiContent nhsApiContent = new NhsApiContent(createKey("valid-content"), getValidConditionResponse(), URL_PREFIX);

        boolean fragmentFound = nhsApiContent.getFragment(NhsApiContent.
                ContentCoordinates.builder()
                .sectionHeading("A section heading that does not exist")
                .mainEntityPosition(0)
                .build()).isPresent();

        assertThat(fragmentFound,
                is(false));
    }

    @Test
    public void get_main_entity_position_doesnt_exist() throws Exception {
        NhsApiContent nhsApiContent = new NhsApiContent(createKey("valid-content"), getValidConditionResponse(), URL_PREFIX);

        boolean fragmentFound = nhsApiContent.getFragment(NhsApiContent.
                ContentCoordinates.builder()
                .sectionHeading("One Main Entity, No Children")
                .mainEntityPosition(2)
                .build()).isPresent();

        assertThat(fragmentFound,
                is(false));
    }

    @Test
    public void urls_are_correctly_prefixed() throws Exception {
        NhsApiContent nhsApiContent = new NhsApiContent(createKey("valid-content"), getValidConditionResponse(), URL_PREFIX);

        String fragment = nhsApiContent.getFragment(NhsApiContent.
                ContentCoordinates.builder()
                .sectionHeading("Section with Link")
                .mainEntityPosition(0)
                .build()).get();

        assertThat(fragment,
                is("<p>Some content with a <a href=\"" + URL_PREFIX + "/path/to/link.html\">link</a> to somewhere</p>"));
    }

    @Test
    public void get_tracker_no_trackers() throws Exception {
        NhsContentKey key = createKey("valid-content");
        NhsApiContent nhsApiContent = new NhsApiContent(key, getValidConditionResponseNoTracker(), URL_PREFIX);

        assertThat(nhsApiContent.getTrackerHtml(),empty());
    }

    @Test
    public void get_tracker_single_tracker() throws Exception {
        NhsContentKey key = createKey("valid-content");
        NhsApiContent nhsApiContent = new NhsApiContent(key, getValidConditionResponse(), URL_PREFIX);

        List<String> html = nhsApiContent.getTrackerHtml();

        assertThat(html, hasSize(1));
        assertThat(html, hasItem("<img style='border: 0; width: 1px; height: 1px;' alt='' src='https://localhost/dcs2221tai1ckz5huxw0mfq86_1m2w/njs.gif?dcsuri=example/flu&wt.cg_n=Syndication'/> <img style='border: 0; width: 1px; height: 1px;' alt='' src='https://localhost/b/ss/nhsuk-prod/1/JS-2.9.0-L8UK/654321?c27=e5423fc0-1b18-11e9-a292-29e11fde9604'/>"));
    }

    @Test
    public void get_tracker_multiple_trackers() throws Exception {
        NhsContentKey key = createKey("valid-content");
        NhsApiContent nhsApiContent = new NhsApiContent(key, getValidConditionResponseManyTrackers(), URL_PREFIX);

        List<String> html = nhsApiContent.getTrackerHtml();

        assertThat(html, hasSize(2));
        assertThat(html, hasItem("<img style='border: 0; width: 1px; height: 1px;' alt='' src='https://localhost/dcs2221tai1ckz5huxw0mfq86_1m2w/njs.gif?dcsuri=example/flu&wt.cg_n=Syndication'/> <img style='border: 0; width: 1px; height: 1px;' alt='' src='https://localhost/b/ss/nhsuk-prod/1/JS-2.9.0-L8UK/654321?c27=e5423fc0-1b18-11e9-a292-29e11fde9604'/>"));
        assertThat(html, hasItem("<img style='border: 0; width: 1px; height: 1px;' alt='' src='https://localhost/othertracker/njs.gif?dcsuri=example/flu&wt.cg_n=Syndication'/> <img style='border: 0; width: 1px; height: 1px;' alt='' src='https://othertracker/b/ss/nhsuk-prod/1/JS-2.9.0-L8UK/654321?c27=e5423fc0-1b18-11e9-a292-29e11fde9604'/>"));
    }
}
