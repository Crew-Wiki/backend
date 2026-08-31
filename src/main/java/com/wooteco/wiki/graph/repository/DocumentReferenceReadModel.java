package com.wooteco.wiki.graph.repository;

import java.util.UUID;

public record DocumentReferenceReadModel(
        UUID sourceDocumentUuid,
        UUID targetDocumentUuid
) {
}
