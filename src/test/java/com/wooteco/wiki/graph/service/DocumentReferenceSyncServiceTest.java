package com.wooteco.wiki.graph.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.graph.domain.DocumentReference;
import com.wooteco.wiki.graph.dto.DocumentReferenceSyncResult;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:reference-sync")
class DocumentReferenceSyncServiceTest {
    @Autowired
    private DocumentReferenceSyncService documentReferenceSyncService;

    @Autowired
    private DocumentReferenceRepository documentReferenceRepository;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Nested
    class Synchronize {

        @Test
        void synchronize_success_byFilteringInvalidTargets() {
            // given
            CrewDocument crewTargetDocument = saveCrewDocument("crew-target", "contents");
            OrganizationDocument organizationTargetDocument = saveOrganizationDocument("organization-target");
            UUID sourceDocumentUuid = UUID.randomUUID();
            UUID missingTargetDocumentUuid = UUID.randomUUID();
            String contents = String.join(
                    " ",
                    createLink(crewTargetDocument.getUuid()),
                    createLink(crewTargetDocument.getUuid()),
                    createLink(organizationTargetDocument.getUuid()),
                    createLink(sourceDocumentUuid),
                    createLink(missingTargetDocumentUuid)
            );
            CrewDocument sourceDocument = saveCrewDocument(
                    "source",
                    contents,
                    sourceDocumentUuid
            );

            // when
            DocumentReferenceSyncResult result = documentReferenceSyncService.synchronize(sourceDocument);
            Set<UUID> savedTargetDocumentUuids = findTargetDocumentUuids(sourceDocument);

            // then
            assertSoftly(softly -> {
                softly.assertThat(savedTargetDocumentUuids).containsExactlyInAnyOrder(
                        crewTargetDocument.getUuid(),
                        organizationTargetDocument.getUuid()
                );
                softly.assertThat(result.expectedTargetDocumentUuids()).isEqualTo(savedTargetDocumentUuids);
                softly.assertThat(result.extractedCount()).isEqualTo(4);
                softly.assertThat(result.validCount()).isEqualTo(2);
                softly.assertThat(result.addedCount()).isEqualTo(2);
                softly.assertThat(result.removedCount()).isZero();
                softly.assertThat(result.excludedSelfCount()).isEqualTo(1);
                softly.assertThat(result.excludedMissingCount()).isEqualTo(1);
            });
        }

        @Test
        void synchronize_success_byChangedAndUnchangedTargets() {
            // given
            CrewDocument firstTargetDocument = saveCrewDocument("first-target", "contents");
            CrewDocument retainedTargetDocument = saveCrewDocument("retained-target", "contents");
            CrewDocument addedTargetDocument = saveCrewDocument("added-target", "contents");
            CrewDocument sourceDocument = saveCrewDocument(
                    "source",
                    createLink(firstTargetDocument.getUuid()) + " " + createLink(retainedTargetDocument.getUuid())
            );
            documentReferenceSyncService.synchronize(sourceDocument);
            sourceDocument.update(
                    "source",
                    createLink(retainedTargetDocument.getUuid()) + " " + createLink(addedTargetDocument.getUuid()),
                    "writer",
                    10L,
                    LocalDateTime.now()
            );

            // when
            DocumentReferenceSyncResult changedResult = documentReferenceSyncService.synchronize(sourceDocument);
            DocumentReferenceSyncResult unchangedResult = documentReferenceSyncService.synchronize(sourceDocument);
            Set<UUID> savedTargetDocumentUuids = findTargetDocumentUuids(sourceDocument);

            // then
            assertSoftly(softly -> {
                softly.assertThat(savedTargetDocumentUuids).containsExactlyInAnyOrder(
                        retainedTargetDocument.getUuid(),
                        addedTargetDocument.getUuid()
                );
                softly.assertThat(changedResult.addedCount()).isEqualTo(1);
                softly.assertThat(changedResult.removedCount()).isEqualTo(1);
                softly.assertThat(unchangedResult.addedCount()).isZero();
                softly.assertThat(unchangedResult.removedCount()).isZero();
            });
        }
    }

    private CrewDocument saveCrewDocument(
            String title,
            String contents
    ) {
        return saveCrewDocument(title, contents, UUID.randomUUID());
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

    private Set<UUID> findTargetDocumentUuids(CrewDocument sourceDocument) {
        List<DocumentReference> references = documentReferenceRepository.findAllBySourceDocument(sourceDocument);
        return references.stream()
                .map(DocumentReference::getTargetDocument)
                .map(document -> document.getUuid())
                .collect(Collectors.toSet());
    }

    private String createLink(UUID documentUuid) {
        return "https://crew-wiki.site/wiki/" + documentUuid;
    }
}
