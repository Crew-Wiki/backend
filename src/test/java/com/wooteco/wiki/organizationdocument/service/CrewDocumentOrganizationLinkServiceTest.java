package com.wooteco.wiki.organizationdocument.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.DocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.DocumentOrganizationLinkRepository;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CrewDocumentOrganizationLinkServiceTest {

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkService documentOrgDocLinkService;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkRepository documentOrgDocLinkRepository;

    @DisplayName("문서 와 조직 문서 연결을 할 때에")
    @Nested
    class Link {

        private CrewDocument savedCrewDocument;
        private OrganizationDocument savedOrganizationDocument;

        @BeforeEach
        void setUp() {
            CrewDocument crewDocument = DocumentFixture.createDefaultCrewDocument();
            savedCrewDocument = crewDocumentRepository.save(crewDocument);

            OrganizationDocument organizationDocument = OrganizationDocumentFixture.createDefault();
            savedOrganizationDocument = organizationDocumentRepository.save(organizationDocument);
        }

        @DisplayName("특정 문서와 특정 조직 문서로 둘을 연결한다.")
        @Test
        void link_success_byCrewAndOrganizationDocument() {
            // when
            documentOrgDocLinkService.link(savedCrewDocument, savedOrganizationDocument);

            // then
            DocumentOrganizationLink documentOrgDocLink = documentOrgDocLinkRepository.findByCrewDocumentAndOrganizationDocument(
                    savedCrewDocument, savedOrganizationDocument).get();

            assertSoftly(softly -> {
                softly.assertThat(documentOrgDocLink.getCrewDocument().getId())
                        .isEqualTo(savedCrewDocument.getId());
                softly.assertThat(documentOrgDocLink.getOrganizationDocument().getId())
                        .isEqualTo(savedOrganizationDocument.getId());
            });
        }
    }

    @DisplayName("문서와 조직 문서 연결 해제 할 때에")
    @Nested
    class Unlink {

        private CrewDocument savedCrewDocument;
        private OrganizationDocument savedOrganizationDocument;

        @BeforeEach
        void setUp() {
            CrewDocument crewDocument = DocumentFixture.createDefaultCrewDocument();
            savedCrewDocument = crewDocumentRepository.save(crewDocument);

            OrganizationDocument organizationDocument = OrganizationDocumentFixture.createDefault();
            savedOrganizationDocument = organizationDocumentRepository.save(organizationDocument);

            documentOrgDocLinkService.link(savedCrewDocument, savedOrganizationDocument);
        }

        @DisplayName("특정 문서와 특정 조직 문서의 연결을 해제한다")
        @Test
        void unlink_success_byCrewAndOrganizationDocument() {
            // when
            documentOrgDocLinkService.unlink(savedCrewDocument, savedOrganizationDocument);

            // then
            Optional<DocumentOrganizationLink> foundDocumentOrgDocLink = documentOrgDocLinkRepository.findByCrewDocumentAndOrganizationDocument(
                    savedCrewDocument, savedOrganizationDocument);

            assertThat(foundDocumentOrgDocLink).isEmpty();
        }

    }
}
