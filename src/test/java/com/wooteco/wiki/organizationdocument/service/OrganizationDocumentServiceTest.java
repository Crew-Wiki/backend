package com.wooteco.wiki.organizationdocument.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.dto.request.OrganizationDocumentLinkRequest;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.DocumentOrganizationLinkRepository;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OrganizationDocumentServiceTest {

    @Autowired
    private OrganizationDocumentService organizationDocumentService;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentOrganizationLinkRepository documentOrganizationLinkRepository;

    @DisplayName("기존 조직 문서를 크루 문서에 연결할 때")
    @Nested
    class LinkExistingOrganization {

        private CrewDocument savedCrewDocument;
        private OrganizationDocument savedOrganizationDocument;

        @BeforeEach
        void setUp() {
            CrewDocument crewDocument = CrewDocumentFixture.createDefaultCrewDocument();
            savedCrewDocument = crewDocumentRepository.save(crewDocument);

            OrganizationDocument organizationDocument = OrganizationDocumentFixture.createDefault();
            savedOrganizationDocument = organizationDocumentRepository.save(organizationDocument);
        }

        @DisplayName("성공적으로 기존 조직 문서를 크루 문서와 연결한다")
        @Test
        void link_success() {
            // given
            OrganizationDocumentLinkRequest request = new OrganizationDocumentLinkRequest(
                savedCrewDocument.getUuid(),
                savedOrganizationDocument.getUuid()
            );

            // when
            organizationDocumentService.link(request);

            // then
            DocumentOrganizationLink link = documentOrganizationLinkRepository
                .findByCrewDocumentAndOrganizationDocument(savedCrewDocument, savedOrganizationDocument)
                .orElseThrow();

            assertSoftly(softly -> {
                softly.assertThat(link.getCrewDocument().getId()).isEqualTo(savedCrewDocument.getId());
                softly.assertThat(link.getOrganizationDocument().getId()).isEqualTo(savedOrganizationDocument.getId());
            });
        }

        @DisplayName("존재하지 않는 크루 문서 UUID로 연결을 시도하면 예외가 발생한다")
        @Test
        void link_fail_crewDocumentNotFound() {
            // given
            OrganizationDocumentLinkRequest request = new OrganizationDocumentLinkRequest(
                UUID.randomUUID(),
                savedOrganizationDocument.getUuid()
            );

            // when & then
            assertThatThrownBy(() -> organizationDocumentService.link(request))
                .isInstanceOf(WikiException.class);
        }

        @DisplayName("존재하지 않는 조직 문서 UUID로 연결을 시도하면 예외가 발생한다")
        @Test
        void link_fail_organizationDocumentNotFound() {
            // given
            OrganizationDocumentLinkRequest request = new OrganizationDocumentLinkRequest(
                savedCrewDocument.getUuid(),
                UUID.randomUUID()
            );

            // when & then
            assertThatThrownBy(() -> organizationDocumentService.link(request))
                .isInstanceOf(WikiException.class);
        }
    }
}
