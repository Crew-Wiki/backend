package com.wooteco.wiki.graph.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class CrewDocumentReferenceExtractorTest {

    private static final UUID FIRST_DOCUMENT_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_DOCUMENT_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    private final CrewDocumentReferenceExtractor crewDocumentReferenceExtractor =
            new CrewDocumentReferenceExtractor();

    @Nested
    @DisplayName("크루 문서 참조 UUID를 추출할 때")
    class Extract {

        @Test
        @DisplayName("정식 주소로 작성한 Markdown 링크와 일반 URL에서 UUID를 추출한다.")
        void extract_success_byCanonicalLinks() {
            // given
            String contents = """
                    [첫 번째 크루](https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111)
                    관련 문서: https://crew-wiki.site/wiki/22222222-2222-2222-2222-222222222222
                    """;

            // when
            List<UUID> references = crewDocumentReferenceExtractor.extract(contents);

            // then
            assertThat(references).containsExactly(FIRST_DOCUMENT_UUID, SECOND_DOCUMENT_UUID);
        }

        @Test
        @DisplayName("동일한 문서를 여러 번 참조하면 UUID를 한 번만 반환한다.")
        void extract_success_byDuplicateLinks() {
            // given
            String contents = """
                    https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111
                    [같은 크루](https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111)
                    """;

            // when
            List<UUID> references = crewDocumentReferenceExtractor.extract(contents);

            // then
            assertThat(references).containsExactly(FIRST_DOCUMENT_UUID);
        }

        @Test
        @DisplayName("외부 주소와 이미지 및 유효하지 않은 문서 경로는 제외한다.")
        void extract_success_byInvalidLinks() {
            // given
            String contents = """
                    https://example.com/wiki/11111111-1111-1111-1111-111111111111
                    https://api.crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111
                    https://www.crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111
                    https://dev.crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111
                    http://localhost:3000/wiki/11111111-1111-1111-1111-111111111111
                    /wiki/11111111-1111-1111-1111-111111111111
                    ![이미지](https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111)
                    /wiki/not-a-uuid
                    /document/uuid/11111111-1111-1111-1111-111111111111
                    11111111-1111-1111-1111-111111111111
                    https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111suffix
                    """;

            // when
            List<UUID> references = crewDocumentReferenceExtractor.extract(contents);

            // then
            assertThat(references).isEmpty();
        }

        @Test
        @DisplayName("본문이 없거나 공백이면 빈 목록을 반환한다.")
        void extract_success_byEmptyContents() {
            // when
            List<UUID> nullContentsReferences = crewDocumentReferenceExtractor.extract(null);
            List<UUID> blankContentsReferences = crewDocumentReferenceExtractor.extract(" ");

            // then
            assertSoftly(softly -> {
                softly.assertThat(nullContentsReferences).isEmpty();
                softly.assertThat(blankContentsReferences).isEmpty();
            });
        }
    }
}
