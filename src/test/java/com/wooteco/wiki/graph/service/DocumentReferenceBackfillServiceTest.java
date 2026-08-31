package com.wooteco.wiki.graph.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doAnswer;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.global.exception.ErrorCode;
import com.wooteco.wiki.global.exception.WikiException;
import com.wooteco.wiki.graph.domain.DocumentReference;
import com.wooteco.wiki.graph.dto.DocumentReferenceBackfillFailure;
import com.wooteco.wiki.graph.dto.DocumentReferenceBackfillResult;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:reference-backfill")
class DocumentReferenceBackfillServiceTest {
    private static final int SOURCE_DOCUMENT_COUNT = 5;
    private static final int TOTAL_DOCUMENT_COUNT = SOURCE_DOCUMENT_COUNT + 1;

    @Autowired
    private DocumentReferenceBackfillService documentReferenceBackfillService;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private DocumentReferenceRepository documentReferenceRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @MockitoSpyBean
    private DocumentReferenceSyncService documentReferenceSyncService;

    @Nested
    class Backfill {

        @Test
        void backfill_success_byEmptyDocuments() {
            // when
            DocumentReferenceBackfillResult result = documentReferenceBackfillService.backfill(2);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.processedCount()).isZero();
                softly.assertThat(result.succeededCount()).isZero();
                softly.assertThat(result.failedCount()).isZero();
                softly.assertThat(result.addedCount()).isZero();
                softly.assertThat(result.mismatchCount()).isZero();
                softly.assertThat(result.failures()).isEmpty();
            });
        }

        @Test
        void backfill_success_byDocumentsSmallerThanPage() {
            // given
            CrewDocument targetDocument = saveTargetDocument();
            List<CrewDocument> sourceDocuments = saveSourceDocuments(targetDocument);

            // when
            DocumentReferenceBackfillResult result = documentReferenceBackfillService.backfill(10);

            // then
            assertAllSourcesReferenceTarget(result, sourceDocuments, targetDocument);
        }

        @Test
        void backfill_success_byDocumentsAcrossPages() {
            // given
            CrewDocument targetDocument = saveTargetDocument();
            List<CrewDocument> sourceDocuments = saveSourceDocuments(targetDocument);

            // when
            DocumentReferenceBackfillResult result = documentReferenceBackfillService.backfill(4);

            // then
            assertAllSourcesReferenceTarget(result, sourceDocuments, targetDocument);
        }

        @Test
        void backfill_success_byDocumentsExactlyPageMultiple() {
            // given
            CrewDocument targetDocument = saveTargetDocument();
            List<CrewDocument> sourceDocuments = saveSourceDocuments(targetDocument);

            // when
            DocumentReferenceBackfillResult result = documentReferenceBackfillService.backfill(3);

            // then
            assertAllSourcesReferenceTarget(result, sourceDocuments, targetDocument);
        }

        @Test
        void backfill_success_bySecondExecution() {
            // given
            CrewDocument targetDocument = saveTargetDocument();
            List<CrewDocument> sourceDocuments = saveSourceDocuments(targetDocument);
            documentReferenceBackfillService.backfill(4);

            // when
            DocumentReferenceBackfillResult result = documentReferenceBackfillService.backfill(4);

            // then
            assertSoftly(softly -> {
                softly.assertThat(result.processedCount()).isEqualTo(TOTAL_DOCUMENT_COUNT);
                softly.assertThat(result.succeededCount()).isEqualTo(TOTAL_DOCUMENT_COUNT);
                softly.assertThat(result.failedCount()).isZero();
                softly.assertThat(result.addedCount()).isZero();
                softly.assertThat(result.removedCount()).isZero();
                softly.assertThat(result.mismatchCount()).isZero();
                softly.assertThat(findTargetDocumentUuidsOf(sourceDocuments))
                        .containsOnly(Set.of(targetDocument.getUuid()));
            });
        }

        @Test
        void backfill_success_byContinuingAfterItemFailure() {
            // given
            CrewDocument staleTargetDocument = saveCrewDocument("stale-target", "contents");
            CrewDocument nextTargetDocument = saveCrewDocument("next-target", "contents");
            CrewDocument healthyTargetDocument = saveCrewDocument("healthy-target", "contents");
            CrewDocument failingSourceDocument = saveCrewDocument(
                    "failing-source",
                    createLink(staleTargetDocument.getUuid())
            );
            CrewDocument healthySourceDocument = saveCrewDocument(
                    "healthy-source",
                    createLink(healthyTargetDocument.getUuid())
            );
            documentReferenceBackfillService.backfill(10);
            updateContents(failingSourceDocument, createLink(nextTargetDocument.getUuid()));
            throwAfterSynchronizing(failingSourceDocument.getUuid());

            // when
            DocumentReferenceBackfillResult result = documentReferenceBackfillService.backfill(2);

            // then
            List<UUID> failedSourceDocumentUuids = result.failures()
                    .stream()
                    .map(DocumentReferenceBackfillFailure::sourceDocumentUuid)
                    .toList();
            assertSoftly(softly -> {
                softly.assertThat(result.processedCount()).isEqualTo(5);
                softly.assertThat(result.succeededCount()).isEqualTo(4);
                softly.assertThat(result.failedCount()).isEqualTo(1);
                softly.assertThat(failedSourceDocumentUuids)
                        .containsExactly(failingSourceDocument.getUuid());
                softly.assertThat(result.failures().get(0).causeType())
                        .isEqualTo("DataIntegrityViolationException");
                softly.assertThat(findTargetDocumentUuids(failingSourceDocument))
                        .containsExactly(staleTargetDocument.getUuid());
                softly.assertThat(findTargetDocumentUuids(healthySourceDocument))
                        .containsExactly(healthyTargetDocument.getUuid());
            });
        }

        @Test
        void backfill_fail_byNonPositivePageSize() {
            // when & then
            assertSoftly(softly -> {
                softly.assertThatThrownBy(() -> documentReferenceBackfillService.backfill(0))
                        .isInstanceOf(WikiException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAGE_BAD_REQUEST);
                softly.assertThatThrownBy(() -> documentReferenceBackfillService.backfill(-1))
                        .isInstanceOf(WikiException.class)
                        .hasFieldOrPropertyWithValue("errorCode", ErrorCode.PAGE_BAD_REQUEST);
            });
        }
    }

    private void assertAllSourcesReferenceTarget(
            DocumentReferenceBackfillResult result,
            List<CrewDocument> sourceDocuments,
            CrewDocument targetDocument
    ) {
        assertSoftly(softly -> {
            softly.assertThat(result.processedCount()).isEqualTo(TOTAL_DOCUMENT_COUNT);
            softly.assertThat(result.succeededCount()).isEqualTo(TOTAL_DOCUMENT_COUNT);
            softly.assertThat(result.failedCount()).isZero();
            softly.assertThat(result.addedCount()).isEqualTo(SOURCE_DOCUMENT_COUNT);
            softly.assertThat(result.removedCount()).isZero();
            softly.assertThat(result.mismatchCount()).isZero();
            softly.assertThat(result.failures()).isEmpty();
            softly.assertThat(findTargetDocumentUuidsOf(sourceDocuments))
                    .hasSize(SOURCE_DOCUMENT_COUNT)
                    .containsOnly(Set.of(targetDocument.getUuid()));
        });
    }

    private void throwAfterSynchronizing(UUID sourceDocumentUuid) {
        doAnswer(invocation -> {
            invocation.callRealMethod();
            throw new DataIntegrityViolationException("backfill item failure");
        }).when(documentReferenceSyncService)
                .synchronize(argThat(sourceDocument -> sourceDocument.getUuid().equals(sourceDocumentUuid)));
    }

    private CrewDocument saveTargetDocument() {
        return saveCrewDocument("target", "contents");
    }

    private List<CrewDocument> saveSourceDocuments(CrewDocument targetDocument) {
        List<CrewDocument> sourceDocuments = new ArrayList<>();
        for (int order = 0; order < SOURCE_DOCUMENT_COUNT; order++) {
            sourceDocuments.add(saveCrewDocument("source-" + order, createLink(targetDocument.getUuid())));
        }
        return sourceDocuments;
    }

    private CrewDocument saveCrewDocument(
            String title,
            String contents
    ) {
        CrewDocument crewDocument = CrewDocumentFixture.createCrewDocument(
                title,
                contents,
                "writer",
                10L,
                UUID.randomUUID()
        );
        return crewDocumentRepository.save(crewDocument);
    }

    private void updateContents(
            CrewDocument sourceDocument,
            String contents
    ) {
        transactionTemplate.executeWithoutResult(status -> {
            CrewDocument foundDocument = crewDocumentRepository.findByUuid(sourceDocument.getUuid()).orElseThrow();
            foundDocument.update(
                    foundDocument.getTitle(),
                    contents,
                    foundDocument.getWriter(),
                    foundDocument.getDocumentBytes(),
                    LocalDateTime.now()
            );
        });
    }

    private List<Set<UUID>> findTargetDocumentUuidsOf(List<CrewDocument> sourceDocuments) {
        List<Set<UUID>> targetDocumentUuids = new ArrayList<>();
        for (CrewDocument sourceDocument : sourceDocuments) {
            targetDocumentUuids.add(findTargetDocumentUuids(sourceDocument));
        }
        return targetDocumentUuids;
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
