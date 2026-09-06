package com.wooteco.wiki.graph.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.graph.dto.CrewGraphElements;
import com.wooteco.wiki.graph.fixture.DocumentReferenceFixture;
import com.wooteco.wiki.graph.repository.CrewGraphQueryRepository;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CrewGraphReaderParityTest {

    private static final String GENERATION_TITLE = "8기";
    private static final UUID FIRST_CREW_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_CREW_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private CrewGraphQueryRepository crewGraphQueryRepository;

    @Autowired
    private DocumentReferenceRepository documentReferenceRepository;

    @Autowired
    private CrewDocumentReferenceExtractor crewDocumentReferenceExtractor;

    @Autowired
    private CrewGraphEdgeNormalizer crewGraphEdgeNormalizer;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkRepository documentOrganizationLinkRepository;

    @Nested
    @DisplayName("두 읽기 경로로 같은 기수를 조회할 때")
    class Read {

        @Test
        @DisplayName("backfill이 끝난 데이터에서는 node와 edge가 완전히 같다.")
        void read_success_bySameElementsAfterBackfill() {
            // given
            saveGenerationGraph();
            saveReferenceRow();

            // when
            CrewGraphElements contentsParsingElements = createContentsParsingReader().read(GENERATION_TITLE);
            CrewGraphElements persistedElements = createPersistedReader().read(GENERATION_TITLE);

            // then
            assertSoftly(softly -> {
                softly.assertThat(contentsParsingElements.nodes()).isEqualTo(persistedElements.nodes());
                softly.assertThat(contentsParsingElements.edges()).isEqualTo(persistedElements.edges());
                softly.assertThat(persistedElements.nodes()).hasSize(2);
                softly.assertThat(persistedElements.edges()).hasSize(1);
            });
        }

        @Test
        @DisplayName("backfill 이전 데이터에서는 영속 참조 경로만 간선을 잃는다.")
        void read_success_byEmptyPersistedEdgesBeforeBackfill() {
            // given
            saveGenerationGraph();

            // when
            CrewGraphElements contentsParsingElements = createContentsParsingReader().read(GENERATION_TITLE);
            CrewGraphElements persistedElements = createPersistedReader().read(GENERATION_TITLE);

            // then
            assertSoftly(softly -> {
                softly.assertThat(contentsParsingElements.nodes()).isEqualTo(persistedElements.nodes());
                softly.assertThat(contentsParsingElements.edges()).hasSize(1);
                softly.assertThat(persistedElements.edges()).isEmpty();
            });
        }
    }

    private CrewGraphReader createContentsParsingReader() {
        return new ContentsParsingCrewGraphReader(
                crewGraphQueryRepository,
                crewDocumentReferenceExtractor,
                crewGraphEdgeNormalizer
        );
    }

    private CrewGraphReader createPersistedReader() {
        return new PersistedCrewGraphReader(
                crewGraphQueryRepository,
                documentReferenceRepository,
                crewGraphEdgeNormalizer
        );
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

    private void saveReferenceRow() {
        CrewDocument firstCrew = crewDocumentRepository.findByUuid(FIRST_CREW_UUID).orElseThrow();
        CrewDocument secondCrew = crewDocumentRepository.findByUuid(SECOND_CREW_UUID).orElseThrow();
        documentReferenceRepository.save(DocumentReferenceFixture.create(firstCrew, secondCrew));
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
