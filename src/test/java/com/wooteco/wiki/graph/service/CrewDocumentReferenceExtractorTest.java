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
    private static final UUID UPPERCASE_DOCUMENT_UUID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final String FIRST_DOCUMENT_URL =
            "https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111";

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
        @DisplayName("같은 문서를 다시 참조해도 첫 등장 순서를 유지한다.")
        void extract_success_byFirstAppearanceOrder() {
            // given
            String contents = """
                    https://crew-wiki.site/wiki/22222222-2222-2222-2222-222222222222
                    https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111
                    [다시 두 번째 크루](https://crew-wiki.site/wiki/22222222-2222-2222-2222-222222222222)
                    """;

            // when
            List<UUID> references = crewDocumentReferenceExtractor.extract(contents);

            // then
            assertThat(references).containsExactly(SECOND_DOCUMENT_UUID, FIRST_DOCUMENT_UUID);
        }

        @Test
        @DisplayName("UUID를 대문자로 작성해도 같은 UUID를 추출한다.")
        void extract_success_byUppercaseUuid() {
            // given
            String contents = "https://crew-wiki.site/wiki/AAAAAAAA-BBBB-CCCC-DDDD-EEEEEEEEEEEE";

            // when
            List<UUID> references = crewDocumentReferenceExtractor.extract(contents);

            // then
            assertThat(references).containsExactly(UPPERCASE_DOCUMENT_UUID);
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
        @DisplayName("URL 뒤에 괄호나 문장부호가 이어져도 UUID를 추출한다.")
        void extract_success_byAllowedPunctuationBoundaries() {
            // given
            String closingParenthesis = "참고(" + FIRST_DOCUMENT_URL + ")";
            String comma = FIRST_DOCUMENT_URL + ", 그리고 다른 문서";
            String colon = FIRST_DOCUMENT_URL + ": 첫 번째 크루";
            String koreanPeriod = FIRST_DOCUMENT_URL + "。";

            // when & then
            assertSoftly(softly -> {
                softly.assertThat(crewDocumentReferenceExtractor.extract(closingParenthesis))
                        .containsExactly(FIRST_DOCUMENT_UUID);
                softly.assertThat(crewDocumentReferenceExtractor.extract(comma))
                        .containsExactly(FIRST_DOCUMENT_UUID);
                softly.assertThat(crewDocumentReferenceExtractor.extract(colon))
                        .containsExactly(FIRST_DOCUMENT_UUID);
                softly.assertThat(crewDocumentReferenceExtractor.extract(koreanPeriod))
                        .containsExactly(FIRST_DOCUMENT_UUID);
            });
        }

        @Test
        @DisplayName("UUID 뒤에 query, fragment, 하위 경로가 붙어도 현재는 UUID를 추출한다.")
        void extract_success_byQueryFragmentAndChildPath() {
            // given
            String query = FIRST_DOCUMENT_URL + "?tab=1";
            String fragment = FIRST_DOCUMENT_URL + "#section";
            String childPath = FIRST_DOCUMENT_URL + "/child";

            // when & then
            assertSoftly(softly -> {
                softly.assertThat(crewDocumentReferenceExtractor.extract(query))
                        .containsExactly(FIRST_DOCUMENT_UUID);
                softly.assertThat(crewDocumentReferenceExtractor.extract(fragment))
                        .containsExactly(FIRST_DOCUMENT_UUID);
                softly.assertThat(crewDocumentReferenceExtractor.extract(childPath))
                        .containsExactly(FIRST_DOCUMENT_UUID);
            });
        }

        @Test
        @DisplayName("이미지 주소 앞에 공백이 있으면 현재는 이미지로 판별하지 못해 UUID를 추출한다.")
        void extract_success_byWhitespaceBeforeMarkdownImageUrl() {
            // given
            String contents = "![이미지]( " + FIRST_DOCUMENT_URL + ")";

            // when
            List<UUID> references = crewDocumentReferenceExtractor.extract(contents);

            // then
            assertThat(references).containsExactly(FIRST_DOCUMENT_UUID);
        }

        @Test
        @DisplayName("UUID 바로 뒤에 마침표가 붙으면 현재는 UUID를 추출하지 못한다.")
        void extract_success_byPeriodSuffix() {
            // given
            String contents = FIRST_DOCUMENT_URL + ".";

            // when
            List<UUID> references = crewDocumentReferenceExtractor.extract(contents);

            // then
            assertThat(references).isEmpty();
        }

        @Test
        @DisplayName("UUID 뒤에 문자, 숫자, underscore, tilde, hyphen이 이어지면 제외한다.")
        void extract_success_byInvalidTrailingCharacters() {
            // given
            String letter = FIRST_DOCUMENT_URL + "a";
            String digit = FIRST_DOCUMENT_URL + "1";
            String underscore = FIRST_DOCUMENT_URL + "_";
            String tilde = FIRST_DOCUMENT_URL + "~";
            String hyphen = FIRST_DOCUMENT_URL + "-";

            // when & then
            assertSoftly(softly -> {
                softly.assertThat(crewDocumentReferenceExtractor.extract(letter)).isEmpty();
                softly.assertThat(crewDocumentReferenceExtractor.extract(digit)).isEmpty();
                softly.assertThat(crewDocumentReferenceExtractor.extract(underscore)).isEmpty();
                softly.assertThat(crewDocumentReferenceExtractor.extract(tilde)).isEmpty();
                softly.assertThat(crewDocumentReferenceExtractor.extract(hyphen)).isEmpty();
            });
        }

        @Test
        @DisplayName("자기 자신이나 존재하지 않는 문서를 가리켜도 문법이 유효하면 UUID를 추출한다.")
        void extract_success_bySyntacticallyValidTargets() {
            // given
            String contents = """
                    [자기 자신](https://crew-wiki.site/wiki/11111111-1111-1111-1111-111111111111)
                    [저장되지 않은 문서](https://crew-wiki.site/wiki/22222222-2222-2222-2222-222222222222)
                    """;

            // when
            List<UUID> references = crewDocumentReferenceExtractor.extract(contents);

            // then
            assertThat(references).containsExactly(FIRST_DOCUMENT_UUID, SECOND_DOCUMENT_UUID);
        }

        @Test
        @DisplayName("본문이 없거나 공백이면 빈 목록을 반환한다.")
        void extract_success_byEmptyContents() {
            // when
            List<UUID> nullContentsReferences = crewDocumentReferenceExtractor.extract(null);
            List<UUID> emptyContentsReferences = crewDocumentReferenceExtractor.extract("");
            List<UUID> blankContentsReferences = crewDocumentReferenceExtractor.extract(" ");
            List<UUID> whitespaceContentsReferences = crewDocumentReferenceExtractor.extract("\n\t");

            // then
            assertSoftly(softly -> {
                softly.assertThat(nullContentsReferences).isEmpty();
                softly.assertThat(emptyContentsReferences).isEmpty();
                softly.assertThat(blankContentsReferences).isEmpty();
                softly.assertThat(whitespaceContentsReferences).isEmpty();
            });
        }
    }
}
