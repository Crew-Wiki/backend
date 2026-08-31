package com.wooteco.wiki.document.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import com.wooteco.wiki.admin.service.CrewDocumentService;
import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.dto.CrewDocumentCreateRequest;
import com.wooteco.wiki.document.domain.dto.DocumentResponse;
import com.wooteco.wiki.document.domain.dto.DocumentUpdateRequest;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.graph.domain.DocumentReference;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import com.wooteco.wiki.history.repository.HistoryRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:reference-transaction")
class CrewDocumentReferenceTransactionTest {
    @Autowired
    private CrewDocumentService crewDocumentService;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private HistoryRepository historyRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoSpyBean
    private DocumentReferenceRepository documentReferenceRepository;

    @Nested
    class Update {

        @Test
        void update_fail_byReferenceSynchronizationFailure() {
            // given
            CrewDocument previousTargetDocument = saveCrewDocument("previous-target");
            CrewDocument nextTargetDocument = saveCrewDocument("next-target");
            String previousContents = createLink(previousTargetDocument.getUuid());
            DocumentResponse sourceDocumentResponse = crewDocumentService.create(createRequest(
                    "source",
                    previousContents,
                    UUID.randomUUID()
            ));
            DocumentUpdateRequest request = new DocumentUpdateRequest(
                    "updated-source",
                    createLink(nextTargetDocument.getUuid()),
                    "updated-writer",
                    20L,
                    sourceDocumentResponse.documentUUID()
            );
            doThrow(new DataIntegrityViolationException("reference synchronization failure"))
                    .when(documentReferenceRepository)
                    .saveAll(any());

            // when & then
            assertThatThrownBy(() -> crewDocumentService.update(sourceDocumentResponse.documentUUID(), request))
                    .isInstanceOf(DataIntegrityViolationException.class);
            entityManager.clear();
            CrewDocument rolledBackDocument = crewDocumentRepository.findByUuid(
                    sourceDocumentResponse.documentUUID()
            ).orElseThrow();
            Set<UUID> rolledBackTargetDocumentUuids = findTargetDocumentUuids(rolledBackDocument);
            assertSoftly(softly -> {
                softly.assertThat(rolledBackDocument.getTitle()).isEqualTo("source");
                softly.assertThat(rolledBackDocument.getContents()).isEqualTo(previousContents);
                softly.assertThat(historyRepository.findAll()).hasSize(1);
                softly.assertThat(rolledBackTargetDocumentUuids)
                        .containsExactly(previousTargetDocument.getUuid());
            });
        }
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

    private String createLink(UUID documentUuid) {
        return "https://crew-wiki.site/wiki/" + documentUuid;
    }
}
