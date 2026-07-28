package com.wooteco.wiki.history.fixture;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.history.domain.History;
import java.time.LocalDateTime;

public class HistoryFixture {

    public static History create(
            String title,
            String contents,
            String writer,
            long documentBytes,
            LocalDateTime generateTime,
            CrewDocument crewDocument,
            Long version
    ) {
        return new History(title, contents, writer, documentBytes, generateTime, crewDocument, version);
    }
}
