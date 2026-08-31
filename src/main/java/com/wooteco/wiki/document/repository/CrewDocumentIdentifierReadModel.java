package com.wooteco.wiki.document.repository;

import java.util.UUID;

public record CrewDocumentIdentifierReadModel(
        Long id,
        UUID uuid
) {
}
