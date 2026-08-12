package com.wooteco.wiki.graph.repository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.DocumentType;
import com.wooteco.wiki.document.fixture.CrewDocumentFixture;
import com.wooteco.wiki.document.repository.CrewDocumentRepository;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.graph.domain.DocumentReference;
import com.wooteco.wiki.graph.fixture.DocumentReferenceFixture;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.fixture.OrganizationDocumentFixture;
import com.wooteco.wiki.organizationdocument.repository.OrganizationDocumentRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
class DocumentReferenceRepositoryTest {

    @Autowired
    private DocumentReferenceRepository documentReferenceRepository;

    @Autowired
    private CrewDocumentRepository crewDocumentRepository;

    @Autowired
    private OrganizationDocumentRepository organizationDocumentRepository;

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private EntityManager entityManager;

    @Nested
    class Save {

        @Test
        void save_success_byValidReference() {
            // given
            CrewDocument sourceDocument = saveCrewDocument("source");
            CrewDocument targetDocument = saveCrewDocument("target");
            DocumentReference documentReference = DocumentReferenceFixture.create(
                    sourceDocument,
                    targetDocument
            );

            // when
            documentReferenceRepository.saveAndFlush(documentReference);
            entityManager.clear();
            List<DocumentReference> references = documentReferenceRepository.findAllBySourceDocument(sourceDocument);
            DocumentReference savedReference = references.get(0);

            // then
            assertSoftly(softly -> {
                softly.assertThat(references).hasSize(1);
                softly.assertThat(savedReference.getSourceDocument()).isEqualTo(sourceDocument);
                softly.assertThat(savedReference.getTargetDocument()).isEqualTo(targetDocument);
            });
        }

        @Test
        void save_success_byOrganizationDocumentTarget() {
            // given
            CrewDocument sourceDocument = saveCrewDocument("source");
            OrganizationDocument targetDocument = saveOrganizationDocument("organization-target");
            DocumentReference documentReference = DocumentReferenceFixture.create(
                    sourceDocument,
                    targetDocument
            );

            // when
            documentReferenceRepository.saveAndFlush(documentReference);
            entityManager.clear();
            Document actualTarget = documentReferenceRepository.findAllBySourceDocument(sourceDocument)
                    .get(0)
                    .getTargetDocument();

            // then
            assertSoftly(softly -> {
                softly.assertThat(actualTarget).isEqualTo(targetDocument);
                softly.assertThat(actualTarget.getDocumentType()).isEqualTo(DocumentType.ORGANIZATION);
            });
        }

        @Test
        void save_success_byDifferentTargets() {
            // given
            CrewDocument sourceDocument = saveCrewDocument("source");
            CrewDocument firstTargetDocument = saveCrewDocument("first-target");
            CrewDocument secondTargetDocument = saveCrewDocument("second-target");

            // when
            saveReference(sourceDocument, firstTargetDocument);
            saveAndFlushReference(sourceDocument, secondTargetDocument);
            entityManager.clear();
            List<DocumentReference> references = documentReferenceRepository.findAllBySourceDocument(sourceDocument);

            // then
            assertSoftly(softly -> {
                softly.assertThat(references).hasSize(2);
                softly.assertThat(references)
                        .extracting(DocumentReference::getTargetDocument)
                        .containsExactlyInAnyOrder(firstTargetDocument, secondTargetDocument);
            });
        }

        @Test
        void save_success_bySameTarget() {
            // given
            CrewDocument firstSourceDocument = saveCrewDocument("first-source");
            CrewDocument secondSourceDocument = saveCrewDocument("second-source");
            CrewDocument targetDocument = saveCrewDocument("target");

            // when
            saveReference(firstSourceDocument, targetDocument);
            saveAndFlushReference(secondSourceDocument, targetDocument);
            entityManager.clear();
            List<DocumentReference> references = documentReferenceRepository.findAll();

            // then
            assertSoftly(softly -> {
                softly.assertThat(references).hasSize(2);
                softly.assertThat(references)
                        .extracting(DocumentReference::getSourceDocument)
                        .containsExactlyInAnyOrder(firstSourceDocument, secondSourceDocument);
            });
        }

        @Test
        void save_fail_byDuplicateSourceAndTarget() {
            // given
            CrewDocument sourceDocument = saveCrewDocument("source");
            CrewDocument targetDocument = saveCrewDocument("target");
            saveAndFlushReference(sourceDocument, targetDocument);
            DocumentReference duplicateReference = DocumentReferenceFixture.create(
                    sourceDocument,
                    targetDocument
            );

            // when & then
            assertThatThrownBy(() -> documentReferenceRepository.saveAndFlush(duplicateReference))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    class FindAllBySourceDocument {

        @Test
        void findAllBySourceDocument_success_byExistingReferences() {
            // given
            CrewDocument selectedSourceDocument = saveCrewDocument("selected-source");
            CrewDocument otherSourceDocument = saveCrewDocument("other-source");
            CrewDocument firstTargetDocument = saveCrewDocument("first-target");
            CrewDocument secondTargetDocument = saveCrewDocument("second-target");
            saveReference(selectedSourceDocument, firstTargetDocument);
            saveReference(selectedSourceDocument, secondTargetDocument);
            saveAndFlushReference(otherSourceDocument, firstTargetDocument);
            entityManager.clear();

            // when
            List<DocumentReference> references = documentReferenceRepository.findAllBySourceDocument(
                    selectedSourceDocument
            );

            // then
            assertSoftly(softly -> {
                softly.assertThat(references).hasSize(2);
                softly.assertThat(references)
                        .extracting(DocumentReference::getTargetDocument)
                        .containsExactlyInAnyOrder(firstTargetDocument, secondTargetDocument);
            });
        }
    }

    @Nested
    class DeleteAllBySourceDocument {

        @Test
        void deleteAllBySourceDocument_success_byOutgoingReferences() {
            // given
            CrewDocument selectedSourceDocument = saveCrewDocument("selected-source");
            CrewDocument otherSourceDocument = saveCrewDocument("other-source");
            CrewDocument firstTargetDocument = saveCrewDocument("first-target");
            CrewDocument secondTargetDocument = saveCrewDocument("second-target");
            saveReference(selectedSourceDocument, firstTargetDocument);
            saveReference(selectedSourceDocument, secondTargetDocument);
            saveAndFlushReference(otherSourceDocument, firstTargetDocument);

            // when
            documentReferenceRepository.deleteAllBySourceDocument(selectedSourceDocument);
            documentReferenceRepository.flush();
            entityManager.clear();
            List<DocumentReference> remainingReferences = documentReferenceRepository.findAll();
            DocumentReference remainingReference = remainingReferences.get(0);

            // then
            assertSoftly(softly -> {
                softly.assertThat(remainingReferences).hasSize(1);
                softly.assertThat(remainingReference.getSourceDocument()).isEqualTo(otherSourceDocument);
                softly.assertThat(remainingReference.getTargetDocument()).isEqualTo(firstTargetDocument);
            });
        }
    }

    @Nested
    class DeleteAllByTargetDocument {

        @Test
        void deleteAllByTargetDocument_success_byIncomingReferences() {
            // given
            CrewDocument firstSourceDocument = saveCrewDocument("first-source");
            CrewDocument secondSourceDocument = saveCrewDocument("second-source");
            CrewDocument selectedTargetDocument = saveCrewDocument("selected-target");
            CrewDocument otherTargetDocument = saveCrewDocument("other-target");
            saveReference(firstSourceDocument, selectedTargetDocument);
            saveReference(secondSourceDocument, selectedTargetDocument);
            saveAndFlushReference(firstSourceDocument, otherTargetDocument);

            // when
            documentReferenceRepository.deleteAllByTargetDocument(selectedTargetDocument);
            documentReferenceRepository.flush();
            entityManager.clear();
            List<DocumentReference> remainingReferences = documentReferenceRepository.findAll();
            DocumentReference remainingReference = remainingReferences.get(0);

            // then
            assertSoftly(softly -> {
                softly.assertThat(remainingReferences).hasSize(1);
                softly.assertThat(remainingReference.getSourceDocument()).isEqualTo(firstSourceDocument);
                softly.assertThat(remainingReference.getTargetDocument()).isEqualTo(otherTargetDocument);
            });
        }
    }

    @Nested
    class Delete {

        @Test
        void delete_success_withoutDocumentCascade() {
            // given
            CrewDocument sourceDocument = saveCrewDocument("source");
            CrewDocument targetDocument = saveCrewDocument("target");
            Long sourceDocumentId = sourceDocument.getId();
            Long targetDocumentId = targetDocument.getId();
            DocumentReference documentReference = saveAndFlushReference(sourceDocument, targetDocument);

            // when
            documentReferenceRepository.delete(documentReference);
            documentReferenceRepository.flush();
            entityManager.clear();

            // then
            assertSoftly(softly -> {
                softly.assertThat(documentReferenceRepository.findAll()).isEmpty();
                softly.assertThat(documentRepository.findById(sourceDocumentId)).contains(sourceDocument);
                softly.assertThat(documentRepository.findById(targetDocumentId)).contains(targetDocument);
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

    private DocumentReference saveReference(
            CrewDocument sourceDocument,
            Document targetDocument
    ) {
        DocumentReference documentReference = DocumentReferenceFixture.create(
                sourceDocument,
                targetDocument
        );
        return documentReferenceRepository.save(documentReference);
    }

    private DocumentReference saveAndFlushReference(
            CrewDocument sourceDocument,
            Document targetDocument
    ) {
        DocumentReference documentReference = DocumentReferenceFixture.create(
                sourceDocument,
                targetDocument
        );
        return documentReferenceRepository.saveAndFlush(documentReference);
    }
}
