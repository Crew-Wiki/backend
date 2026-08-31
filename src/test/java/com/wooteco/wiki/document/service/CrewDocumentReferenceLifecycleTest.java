package com.wooteco.wiki.document.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.admin.service.CrewDocumentService;
import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.dto.CrewDocumentCreateRequest;
import com.wooteco.wiki.document.domain.dto.DocumentResponse;
import com.wooteco.wiki.document.domain.dto.DocumentUpdateRequest;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.graph.domain.DocumentReference;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:reference-lifecycle")
class CrewDocumentReferenceLifecycleTest {
    @Autowired
    private CrewDocumentService crewDocumentService;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentReferenceRepository documentReferenceRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Nested
    class Create {

        @Test
        void create_success_byContentsWithValidReference() {
            // given
            CrewDocument targetDocument = saveCrewDocument("target");
            CrewDocumentCreateRequest request = createRequest(
                    "source",
                    createLink(targetDocument.getUuid()),
                    UUID.randomUUID()
            );

            // when
            DocumentResponse response = crewDocumentService.create(request);
            CrewDocument sourceDocument = crewDocumentRepository.findByUuid(response.documentUUID()).orElseThrow();

            // then
            assertSoftly(softly -> {
                softly.assertThat(findTargetDocumentUuids(sourceDocument)).containsExactly(targetDocument.getUuid());
                softly.assertThat(documentRepository.findByUuid(sourceDocument.getUuid())).isPresent();
            });
        }
    }

    @Nested
    class Update {

        @Test
        void update_success_byChangedReference() {
            // given
            CrewDocument previousTargetDocument = saveCrewDocument("previous-target");
            CrewDocument nextTargetDocument = saveCrewDocument("next-target");
            DocumentResponse createdDocument = crewDocumentService.create(createRequest(
                    "source",
                    createLink(previousTargetDocument.getUuid()),
                    UUID.randomUUID()
            ));
            DocumentUpdateRequest request = createUpdateRequest(
                    createdDocument.documentUUID(),
                    createLink(nextTargetDocument.getUuid())
            );

            // when
            crewDocumentService.update(createdDocument.documentUUID(), request);
            CrewDocument sourceDocument = crewDocumentRepository.findByUuid(
                    createdDocument.documentUUID()
            ).orElseThrow();

            // then
            assertSoftly(softly -> {
                softly.assertThat(findTargetDocumentUuids(sourceDocument)).containsExactly(nextTargetDocument.getUuid());
                softly.assertThat(sourceDocument.getContents()).isEqualTo(request.contents());
            });
        }
    }

    @Nested
    class DeleteByUuid {

        @Test
        void deleteByUuid_success_byIncomingAndOutgoingReferences() {
            // given
            CrewDocument outgoingTargetDocument = saveCrewDocument("outgoing-target");
            DocumentResponse deletedDocument = crewDocumentService.create(createRequest(
                    "deleted-source",
                    createLink(outgoingTargetDocument.getUuid()),
                    UUID.randomUUID()
            ));
            crewDocumentService.create(createRequest(
                    "incoming-source",
                    createLink(deletedDocument.documentUUID()),
                    UUID.randomUUID()
            ));

            // when
            crewDocumentService.deleteByUuid(deletedDocument.documentUUID());

            // then
            assertSoftly(softly -> {
                softly.assertThat(documentRepository.findByUuid(deletedDocument.documentUUID())).isEmpty();
                softly.assertThat(documentReferenceRepository.findAll()).isEmpty();
            });
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

    private CrewDocumentCreateRequest createRequest(
            String title,
            String contents,
            UUID uuid
    ) {
        return CrewDocumentFixture.createDocumentCreateRequest(
                title,
                contents,
                "writer",
                10L,
                uuid
        );
    }

    private DocumentUpdateRequest createUpdateRequest(
            UUID sourceDocumentUuid,
            String contents
    ) {
        return new DocumentUpdateRequest(
                "source",
                contents,
                "writer",
                10L,
                sourceDocumentUuid
        );
    }

    private Set<UUID> findTargetDocumentUuids(CrewDocument sourceDocument) {
        return transactionTemplate.execute(status -> {
            List<DocumentReference> references = documentReferenceRepository.findAllBySourceDocument(sourceDocument);
            return references.stream()
                    .map(DocumentReference::getTargetDocument)
                    .map(document -> document.getUuid())
                    .collect(Collectors.toSet());
        });
    }

    private String createLink(UUID documentUuid) {
        return "https://crew-wiki.site/wiki/" + documentUuid;
    }
}
