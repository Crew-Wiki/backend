package com.wooteco.wiki.graph.dto;

import java.util.UUID;

public record DocumentReferenceBackfillFailure(
        UUID sourceDocumentUuid,
        String causeType
) {

    public static DocumentReferenceBackfillFailure of(
            UUID sourceDocumentUuid,
            Throwable cause
    ) {
        return new DocumentReferenceBackfillFailure(
                sourceDocumentUuid,
                cause.getClass().getSimpleName()
        );
    }
}
