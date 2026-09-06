package com.wooteco.wiki.graph.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.graph.dto.CrewGraphResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeResponse;
import com.wooteco.wiki.graph.dto.GraphEdgeType;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.DocumentOrganizationLinkFixture;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.DocumentOrganizationLinkRepository;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

// graph.read.source를 지정하지 않은 기본 상태를 검증한다.
// backfill 전에 배포되어도 그래프가 비지 않도록 기본값은 본문 파싱 경로여야 한다.
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CrewGraphQueryServiceDefaultReadSourceTest {

    private static final String GENERATION_TITLE = "8기";
    private static final UUID FIRST_CREW_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_CREW_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private CrewGraphQueryService crewGraphQueryService;

    @Autowired
    private CrewGraphReader crewGraphReader;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkRepository documentOrganizationLinkRepository;

    @Nested
    @DisplayName("graph.read.source를 지정하지 않고 기수별 크루 그래프를 조회할 때")
    class FindByGeneration {

        @Test
        @DisplayName("본문 파싱 reader가 선택된다.")
        void findByGeneration_success_byContentsParsingReaderAsDefault() {
            // then
            assertSoftly(softly -> softly.assertThat(crewGraphReader)
                    .isInstanceOf(ContentsParsingCrewGraphReader.class));
        }

        @Test
        @DisplayName("document_reference가 한 건도 없어도 본문에서 참조 간선을 만든다.")
        void findByGeneration_success_byContentsEdgesWithoutPersistedReference() {
            // given
            saveGenerationGraph();

            // when
            CrewGraphResponse response = crewGraphQueryService.findByGeneration(GENERATION_TITLE);

            // then
            assertSoftly(softly -> {
                softly.assertThat(response.nodes()).hasSize(2);
                softly.assertThat(response.edges())
                        .containsExactly(new GraphEdgeResponse(
                                FIRST_CREW_UUID,
                                SECOND_CREW_UUID,
                                GraphEdgeType.REFERENCE
                        ));
            });
        }
    }

    private void saveGenerationGraph() {
        OrganizationDocument generation = saveOrganizationDocument(GENERATION_TITLE);
        CrewDocument firstCrew = saveCrewDocument(
                "가람(8기)",
                "나래 문서 https://crew-wiki.site/wiki/" + SECOND_CREW_UUID + " 를 참조한다.",
                FIRST_CREW_UUID
        );
        CrewDocument secondCrew = saveCrewDocument("나래(8기)", "본문", SECOND_CREW_UUID);
        saveLink(firstCrew, generation);
        saveLink(secondCrew, generation);
    }

    private CrewDocument saveCrewDocument(
            String title,
            String contents,
            UUID uuid
    ) {
        CrewDocument crewDocument = CrewDocumentFixture.createCrewDocument(
                title,
                contents,
                "writer",
                10L,
                uuid
        );
        return crewDocumentRepository.save(crewDocument);
    }

    private OrganizationDocument saveOrganizationDocument(String title) {
        OrganizationDocument organizationDocument = OrganizationDocumentFixture.create(
                title,
                "contents",
                "writer",
                10L,
                UUID.randomUUID()
        );
        return organizationDocumentRepository.save(organizationDocument);
    }

    private void saveLink(
            CrewDocument crewDocument,
            OrganizationDocument organizationDocument
    ) {
        DocumentOrganizationLink link = DocumentOrganizationLinkFixture.create(
                crewDocument,
                organizationDocument
        );
        documentOrganizationLinkRepository.save(link);
    }
}
