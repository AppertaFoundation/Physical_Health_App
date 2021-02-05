package com.staircase13.apperta.cms;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.staircase13.apperta.cms.loader.NhsContentKey;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class NhsApiContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(NhsApiContent.class);

    private final DocumentContext documentContext;
    private final NhsContentKey key;
    private final String rawJson;
    private final String urlPrefix;

    /**
     * @param key Identifier for the content
     * @param rawJson JSON API Content
     * @param urlPrefix URL prefix to add to any links in the extracted HTML
     */
    public NhsApiContent(NhsContentKey key,
                         String rawJson,
                         String urlPrefix) throws CannotParseApiContentException{
        this.key = key;
        this.rawJson = rawJson;
        this.documentContext = parseJson(rawJson);
        this.urlPrefix = urlPrefix;
    }

    public NhsContentKey getKey() {
        return key;
    }

    public String getRawJson() {
        return rawJson;
    }

    private DocumentContext parseJson(String content) throws CannotParseApiContentException {
        try {
            return JsonPath.parse(content);
        } catch(com.jayway.jsonpath.InvalidJsonException e) {
            throw new CannotParseApiContentException(String.format("Could not load API Content '%s'",key),e);
        }
    }

    public LocalDateTime getLastModified() {
        String dateModifiedString = documentContext.read("$.dateModified");
        return LocalDateTime.parse(dateModifiedString,DateTimeFormatter.ISO_DATE_TIME);
    }


    public Optional<String> getFragment(ContentCoordinates coordinates) {
        String query = String.format("$..mainEntityOfPage[?(@.name=='section heading' && @.text=='%s')].mainEntityOfPage[?(@.position==%s)]..text",
                coordinates.getSectionHeading().replaceAll("\'","\\\\'"),
                coordinates.getMainEntityPosition());

        LOGGER.debug("Query is '{}'",query);

        List<String> results = documentContext.read(query, List.class);

        if(results.isEmpty()) {
            return Optional.empty();
        } else {
            String htmlFragment = results.get(0);

            Document jsoupDocument = jSoupParse(htmlFragment);
            jsoupDocument.select("a")
                    .forEach(link -> link.attr("href",urlPrefix + link.attr("href")));

            return Optional.of(jsoupDocument.html());
        }
    }

    public List<String> getTrackerHtml() {
        String query = String.format("$..interactionStatistic[?(@.@type=='InteractionCounter')].interactionService[?(@.@type=='Website')].url");

        LOGGER.debug("Trackers query is '{}'",query);

        return documentContext.read(query, List.class);
    }

    private List<String> getImageSrcAttributes(String html) {
        Document jSoupDocument = jSoupParse(html);
        return jSoupDocument
                .select("img")
                .stream()
                .map(element -> element.attr("src"))
                .collect(toList());
    }

    private Document jSoupParse(String fragment) {
        // we use an XML parser because the default HTML parser will add html and body tags
        return Jsoup.parse(fragment,"", Parser.xmlParser());
    }

    @Getter
    @Setter
    @Builder
    @EqualsAndHashCode
    public static class ContentCoordinates {
        private final String sectionHeading;
        private final int mainEntityPosition;
    }

}
