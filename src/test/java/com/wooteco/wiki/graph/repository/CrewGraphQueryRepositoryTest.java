package com.wooteco.wiki.graph.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
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
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class CrewGraphQueryRepositoryTest {

    @Autowired
    private CrewGraphQueryRepository crewGraphQueryRepository;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkRepository documentOrganizationLinkRepository;

    @Nested
    @DisplayName("기수와 조직에 모두 연결된 크루 문서 UUID를 조회할 때")
    class FindAllCrewDocumentUuidsByGenerationTitleAndOrganizationDocumentUuid {

        @Test
        @DisplayName("두 조직에 모두 연결된 크루 문서 UUID만 반환한다.")
        void findAllCrewDocumentUuidsByGenerationTitleAndOrganizationDocumentUuid_success_byMatchingLinks() {
            // given
            OrganizationDocument eighthGeneration = saveOrganizationDocument("8기");
            OrganizationDocument seventhGeneration = saveOrganizationDocument("7기");
            OrganizationDocument backend = saveOrganizationDocument("백엔드");
            CrewDocument eighthBackendCrew = saveCrewDocument("가람(8기)");
            CrewDocument eighthFrontendCrew = saveCrewDocument("나래(8기)");
            CrewDocument seventhBackendCrew = saveCrewDocument("다온(7기)");
            saveLink(eighthBackendCrew, eighthGeneration);
            saveLink(eighthBackendCrew, backend);
            saveLink(eighthFrontendCrew, eighthGeneration);
            saveLink(seventhBackendCrew, seventhGeneration);
            saveLink(seventhBackendCrew, backend);

            // when
            List<UUID> crewDocumentUuids = crewGraphQueryRepository
                    .findAllCrewDocumentUuidsByGenerationTitleAndOrganizationDocumentUuid(
                            "8기",
                            backend.getUuid()
                    );

            // then
            assertThat(crewDocumentUuids).containsExactly(eighthBackendCrew.getUuid());
        }
    }

    private CrewDocument saveCrewDocument(String title) {
        CrewDocument crewDocument = CrewDocumentFixture.createCrewDocument(
                title,
                "contents",
                "writer",
                10L,
                UUID.randomUUID()
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
