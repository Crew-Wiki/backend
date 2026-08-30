package com.wooteco.wiki.document.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.admin.service.CrewDocumentService;
import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.dto.CrewDocumentCreateRequest;
import com.wooteco.wiki.document.domain.dto.DocumentResponse;
import com.wooteco.wiki.document.domain.dto.DocumentUpdateRequest;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.graph.domain.DocumentReference;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:reference-concurrency")
class CrewDocumentReferenceConcurrencyTest {
    @Autowired
    private CrewDocumentService crewDocumentService;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private DocumentReferenceRepository documentReferenceRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Nested
    class Update {

        @Test
        void update_success_byConcurrentRequestsForSameSource() throws Exception {
            // given
            CrewDocument firstTargetDocument = saveCrewDocument("first-target");
            CrewDocument secondTargetDocument = saveCrewDocument("second-target");
            DocumentResponse sourceDocumentResponse = crewDocumentService.create(createRequest(
                    "source",
                    "contents",
                    UUID.randomUUID()
            ));
            DocumentUpdateRequest firstRequest = createUpdateRequest(
                    sourceDocumentResponse.documentUUID(),
                    createLink(firstTargetDocument.getUuid())
            );
            DocumentUpdateRequest secondRequest = createUpdateRequest(
                    sourceDocumentResponse.documentUUID(),
                    createLink(secondTargetDocument.getUuid())
            );
            CountDownLatch readyLatch = new CountDownLatch(2);
            CountDownLatch startLatch = new CountDownLatch(1);
            ExecutorService executorService = Executors.newFixedThreadPool(2);

            try {
                // when
                Future<?> firstUpdate = executorService.submit(
                        () -> updateAfterStart(sourceDocumentResponse.documentUUID(), firstRequest, readyLatch, startLatch)
                );
                Future<?> secondUpdate = executorService.submit(
                        () -> updateAfterStart(sourceDocumentResponse.documentUUID(), secondRequest, readyLatch, startLatch)
                );
                readyLatch.await(5, TimeUnit.SECONDS);
                startLatch.countDown();
                firstUpdate.get(10, TimeUnit.SECONDS);
                secondUpdate.get(10, TimeUnit.SECONDS);

                CrewDocument finalSourceDocument = crewDocumentRepository.findByUuid(
                        sourceDocumentResponse.documentUUID()
                ).orElseThrow();
                Set<UUID> savedTargetDocumentUuids = findTargetDocumentUuids(finalSourceDocument);
                UUID expectedTargetDocumentUuid = findExpectedTargetDocumentUuid(
                        finalSourceDocument,
                        firstTargetDocument,
                        secondTargetDocument
                );

                // then
                assertSoftly(softly -> {
                    softly.assertThat(savedTargetDocumentUuids).containsExactly(expectedTargetDocumentUuid);
                    softly.assertThat(finalSourceDocument.getContents())
                            .isEqualTo(createLink(expectedTargetDocumentUuid));
                });
            } finally {
                executorService.shutdownNow();
            }
        }
    }

    private void updateAfterStart(
            UUID sourceDocumentUuid,
            DocumentUpdateRequest request,
            CountDownLatch readyLatch,
            CountDownLatch startLatch
    ) {
        readyLatch.countDown();
        awaitStart(startLatch);
        crewDocumentService.update(sourceDocumentUuid, request);
    }

    private void awaitStart(CountDownLatch startLatch) {
        try {
            startLatch.await();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new AssertionError(exception);
        }
    }

    private UUID findExpectedTargetDocumentUuid(
            CrewDocument sourceDocument,
            CrewDocument firstTargetDocument,
            CrewDocument secondTargetDocument
    ) {
        if (sourceDocument.getContents().contains(firstTargetDocument.getUuid().toString())) {
            return firstTargetDocument.getUuid();
        }
        return secondTargetDocument.getUuid();
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

    private String createLink(UUID documentUuid) {
        return "https://crew-wiki.site/wiki/" + documentUuid;
    }
}
