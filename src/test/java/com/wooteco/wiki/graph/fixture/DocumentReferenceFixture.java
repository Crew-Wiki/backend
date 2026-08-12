package com.wooteco.wiki.graph.fixture;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.graph.domain.DocumentReference;

public final class DocumentReferenceFixture {

    public static DocumentReference create(
            CrewDocument sourceDocument,
            Document targetDocument
    ) {
        return DocumentReference.of(sourceDocument, targetDocument);
    }

    private DocumentReferenceFixture() {
    }
}
