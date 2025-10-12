package com.wooteco.wiki.document.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.DocumentFixture;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.dto.OrganizationDocumentSearchResponse;
import com.wooteco.wiki.organizationdocument.dto.response.OrganizationDocumentResponse;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import com.wooteco.wiki.organizationdocument.service.DocumentOrganizationLinkService;
import java.util.List;
import java.util.UUID;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CrewDocumentServiceJavaTest {

    @Autowired
    private DocumentServiceJava documentService;

    @Autowired
    private DocumentOrganizationLinkService documentOrganizationLinkService;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    private CrewDocument savedCrewDocument;
    private UUID savedDocumentUuid;

    @BeforeEach
    void setUp() {
        CrewDocument crewDocument = DocumentFixture.createDefaultCrewDocument();
        savedCrewDocument = documentRepository.save(crewDocument);
        savedDocumentUuid = savedCrewDocument.getUuid();
    }

    @DisplayName("특정 문서에 대한 조직 문서 제목과 Uuid들을 조회할 때")
    @Nested
    class searchOrganizationCrewDocument {

        @BeforeEach
        void setUp() {
            List<OrganizationDocument> organizationDocuments = List.of(
                    OrganizationDocumentFixture.create("title1", "defaultContent", "defaultWriter", 10L,
                            UUID.randomUUID()),
                    OrganizationDocumentFixture.create("title2", "defaultContent", "defaultWriter", 10L,
                            UUID.randomUUID())
            );
            organizationDocumentRepository.saveAll(organizationDocuments);

            for (OrganizationDocument organizationDocument : organizationDocuments) {
                documentOrganizationLinkService.link(savedCrewDocument, organizationDocument);
            }
        }

        @DisplayName("존재하는 특정 문서의 Uuid로 요청한다면 조직 문서들이 올바르게 조회된다")
        @Test
        void searchOrganizationDocument_success_byExistingDocumentUuid() {
            // when
            List<OrganizationDocumentSearchResponse> organizationDocumentSearchResponse = documentService.searchOrganizationDocument(
                    savedDocumentUuid);

            // then
            assertSoftly(softy -> {
                        softy.assertThat(organizationDocumentSearchResponse.size()).isEqualTo(2);
                        softy.assertThat(organizationDocumentSearchResponse.stream()
                                .map(OrganizationDocumentSearchResponse::title)
                        ).containsExactlyInAnyOrder("title1", "title2");
                    }
            );
        }

        @DisplayName("존재하지 않는 특정 문서의 Uuid로 요청한다면 예외가 발생한다 : DOCUMENT_NOT_FOUND")
        @Test
        void searchOrganizationDocument_error_byNonExistingDocumentUuid() {
            // when & then
            WikiException ex = assertThrows(WikiException.class,
                    () -> documentService.searchOrganizationDocument(UUID.randomUUID()));
            Assertions.assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND);
        }
    }

    @DisplayName("특정 문서에 대해 조직 문서를 제거할 때에")
    @Nested
    class deleteOrganizationCrewDocument {

        private UUID savedOrganizationDocumentUuid;

        @BeforeEach
        void setUp() {
            OrganizationDocument organizationDocument = OrganizationDocumentFixture.create("title1", "defaultContent",
                    "defaultWriter", 10L, UUID.randomUUID());
            OrganizationDocument savedOrganizationDocument = organizationDocumentRepository.save(organizationDocument);
            documentOrganizationLinkService.link(savedCrewDocument, organizationDocument);

            savedOrganizationDocumentUuid = savedOrganizationDocument.getUuid();
        }

        @DisplayName("존재하는 특정 문서의 uuid로 요청한다면, 조직 문서가 삭제된다")
        @Test
        void deleteOrganizationDocument_success_byExistingDocumentUuid() {
            // when
            documentService.deleteOrganizationDocument(savedDocumentUuid, savedOrganizationDocumentUuid);

            // then
            List<OrganizationDocumentResponse> organizationDocumentResponsesByDocument = documentOrganizationLinkService.findOrganizationDocumentResponsesByDocument(
                    savedCrewDocument);

            assertThat(organizationDocumentResponsesByDocument.size()).isEqualTo(0);
        }

        @DisplayName("존재하지 않는 특정 문서의 Uuid로 요청한다면 예외가 발생한다 : DOCUMENT_NOT_FOUND")
        @Test
        void deleteOrganizationDocument_error_byNonExistingDocumentUuid() {
            // when & then
            WikiException ex = assertThrows(WikiException.class,
                    () -> documentService.deleteOrganizationDocument(UUID.randomUUID(), savedOrganizationDocumentUuid));
            Assertions.assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DOCUMENT_NOT_FOUND);
        }

        @DisplayName("존재하지 않는 특정 조직 문서의 Uuid로 요청한다면 예외가 발생한다 : ORGANIZATION_DOCUMENT_NOT_FOUND")
        @Test
        void deleteOrganizationDocument_error_byNonExistingOrganizationDocumentUuid() {
            // when & then
            WikiException ex = assertThrows(WikiException.class,
                    () -> documentService.deleteOrganizationDocument(savedDocumentUuid, UUID.randomUUID()));
            Assertions.assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.ORGANIZATION_DOCUMENT_NOT_FOUND);
        }
    }
}
