package com.wooteco.wiki.document.service;

import com.wooteco.wiki.document.domain.CrewField;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CrewProfileExtractor {

    private static final String NAME_DELIMITER = "(";
    private static final Map<String, CrewField> FIELD_BY_ORGANIZATION_TITLE = Map.of(
            "백엔드", CrewField.BACKEND,
            "프론트엔드", CrewField.FRONTEND,
            "안드로이드", CrewField.ANDROID
    );

    public Optional<String> extractName(String title) {
        int delimiterIndex = title.indexOf(NAME_DELIMITER);
        String extractedName = title;
        if (delimiterIndex >= 0) {
            extractedName = title.substring(0, delimiterIndex);
        }
        String name = extractedName.trim();
        if (name.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(name);
    }

    public Optional<CrewField> extractField(List<String> organizationTitles) {
        Set<CrewField> fields = new HashSet<>();
        for (String organizationTitle : organizationTitles) {
            addFieldIfKnown(fields, organizationTitle);
        }
        if (fields.size() != 1) {
            return Optional.empty();
        }
        return Optional.of(fields.iterator().next());
    }

    private void addFieldIfKnown(
            Set<CrewField> fields,
            String organizationTitle
    ) {
        CrewField field = FIELD_BY_ORGANIZATION_TITLE.get(organizationTitle);
        if (field == null) {
            return;
        }
        fields.add(field);
    }
}
