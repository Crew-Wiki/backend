package com.wooteco.wiki.document.fixture;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.dto.CrewDocumentCreateRequest;
import java.util.UUID;

public class CrewDocumentFixture {

    public static CrewDocument createCrewDocument(String title, String content, String writer, Long documentBytes,
                                                  UUID uuid) {
        return new CrewDocument(title, content, writer, documentBytes, uuid);
    }

    public static CrewDocument createDefaultCrewDocument() {
        return createCrewDocument("defaultCrewTitle", "defaultContent", "defaultWriter", 10L, UUID.randomUUID());
    }

    public static CrewDocumentCreateRequest createDocumentCreateRequest(String title, String contents, String writer,
                                                                        Long documentBytes, UUID uuid) {
        return new CrewDocumentCreateRequest(title, contents, writer, documentBytes, uuid);
    }

    public static CrewDocumentCreateRequest createDocumentCreateRequestDefault() {
        return createDocumentCreateRequest("defaultTitle", "defaultContent", "defaultWriter", 10L, UUID.randomUUID());
    }
}
