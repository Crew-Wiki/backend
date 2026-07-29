package com.wooteco.wiki.graph.service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class CrewDocumentReferenceExtractor {

    private static final String UUID_PATTERN =
            "[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}";
    private static final Pattern INTERNAL_DOCUMENT_LINK_PATTERN = Pattern.compile(
            "(?<![A-Za-z0-9._~:/?#@!$&'*+,;=%-])"
                    + "https://crew-wiki\\.site/wiki/(?<uuid>" + UUID_PATTERN + ")"
                    + "(?=$|[^A-Za-z0-9._~-])"
    );

    public List<UUID> extract(String contents) {
        if (contents == null || contents.isBlank()) {
            return List.of();
        }
        Set<UUID> references = new LinkedHashSet<>();
        Matcher matcher = INTERNAL_DOCUMENT_LINK_PATTERN.matcher(contents);
        while (matcher.find()) {
            addReferenceIfValid(contents, matcher, references);
        }
        return List.copyOf(references);
    }

    private void addReferenceIfValid(
            String contents,
            Matcher matcher,
            Set<UUID> references
    ) {
        if (isMarkdownImage(contents, matcher.start())) {
            return;
        }
        references.add(UUID.fromString(matcher.group("uuid")));
    }

    private boolean isMarkdownImage(
            String contents,
            int linkStart
    ) {
        boolean startsAfterMarkdownLabel = linkStart >= 3
                && contents.charAt(linkStart - 1) == '('
                && contents.charAt(linkStart - 2) == ']';
        if (!startsAfterMarkdownLabel) {
            return false;
        }
        int altTextStart = contents.lastIndexOf('[', linkStart - 2);
        return altTextStart > 0 && contents.charAt(altTextStart - 1) == '!';
    }
}
