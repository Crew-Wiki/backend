package com.wooteco.wiki.graph.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.graph.dto.CrewGraphResponse;
import com.wooteco.wiki.graph.fixture.DocumentReferenceFixture;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.DocumentOrganizationLinkFixture;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.DocumentOrganizationLinkRepository;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:graph-query-count",
        "spring.jpa.properties.hibernate.session_factory.statement_inspector="
                + "com.wooteco.wiki.graph.service.RecordingStatementInspector"
})
class CrewGraphQueryServiceQueryCountTest {

    private static final UUID FIRST_CREW_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SECOND_CREW_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID ORGANIZATION_DOCUMENT_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private CrewGraphQueryService crewGraphQueryService;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkRepository documentOrganizationLinkRepository;

    @Autowired
    private DocumentReferenceRepository documentReferenceRepository;

    @Nested
    @DisplayName("기수별 크루 그래프를 조회할 때")
    class FindByGeneration {

        @Test
        @DisplayName("조직을 선택하지 않으면 node와 reference 조회 2개만 실행하고 contents를 조회하지 않는다.")
        void findByGeneration_success_byTwoQueriesWithoutOrganization() {
            // given
            saveGenerationGraph();
            RecordingStatementInspector.clear();

            // when
            CrewGraphResponse response = crewGraphQueryService.findByGeneration("8기");

            // then
            List<String> statements = RecordingStatementInspector.statements();
            assertSoftly(softly -> {
                softly.assertThat(response.nodes()).hasSize(2);
                softly.assertThat(response.edges()).hasSize(1);
                softly.assertThat(statements).hasSize(2);
                softly.assertThat(statements).noneMatch(statement -> statement.contains("contents"));
            });
        }

        @Test
        @DisplayName("조직을 선택하면 node·reference·조직 조회 4개를 실행한다.")
        void findByGeneration_success_byFourQueriesWithSelectedOrganization() {
            // given
            saveGenerationGraph();
            OrganizationDocument backend = saveOrganizationDocument("백엔드", ORGANIZATION_DOCUMENT_UUID);
            saveLink(findCrewDocument(FIRST_CREW_UUID), backend);
            RecordingStatementInspector.clear();

            // when
            CrewGraphResponse response = crewGraphQueryService.findByGeneration(
                    "8기",
                    ORGANIZATION_DOCUMENT_UUID
            );

            // then
            List<String> statements = RecordingStatementInspector.statements();
            assertSoftly(softly -> {
                softly.assertThat(response.nodes()).hasSize(3);
                softly.assertThat(response.edges()).hasSize(2);
                softly.assertThat(statements).hasSize(4);
            });
        }
    }

    private void saveGenerationGraph() {
        OrganizationDocument generation = saveOrganizationDocument("8기", UUID.randomUUID());
        CrewDocument firstCrew = saveCrewDocument("가람(8기)", FIRST_CREW_UUID);
        CrewDocument secondCrew = saveCrewDocument("나래(8기)", SECOND_CREW_UUID);
        saveLink(firstCrew, generation);
        saveLink(secondCrew, generation);
        saveReference(firstCrew, secondCrew);
    }

    private CrewDocument findCrewDocument(UUID documentUuid) {
        return crewDocumentRepository.findByUuid(documentUuid).orElseThrow();
    }

    private CrewDocument saveCrewDocument(
            String title,
            UUID uuid
    ) {
        CrewDocument crewDocument = CrewDocumentFixture.createCrewDocument(
                title,
                "contents",
                "writer",
                10L,
                uuid
        );
        return crewDocumentRepository.save(crewDocument);
    }

    private OrganizationDocument saveOrganizationDocument(
            String title,
            UUID uuid
    ) {
        OrganizationDocument organizationDocument = OrganizationDocumentFixture.create(
                title,
                "contents",
                "writer",
                10L,
                uuid
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

    private void saveReference(
            CrewDocument sourceDocument,
            CrewDocument targetDocument
    ) {
        documentReferenceRepository.save(DocumentReferenceFixture.create(sourceDocument, targetDocument));
    }
}
