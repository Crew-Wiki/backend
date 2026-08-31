package com.wooteco.wiki.graph.service;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.repository.DocumentRepository;
import com.wooteco.wiki.graph.domain.DocumentReference;
import com.wooteco.wiki.graph.dto.DocumentReferenceSyncResult;
import com.wooteco.wiki.graph.repository.DocumentReferenceRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DocumentReferenceSyncService {
    private final CrewDocumentReferenceExtractor crewDocumentReferenceExtractor;
    private final DocumentRepository documentRepository;
    private final DocumentReferenceRepository documentReferenceRepository;

    @Transactional
    public DocumentReferenceSyncResult synchronize(CrewDocument sourceDocument) {
        List<UUID> extractedTargetDocumentUuids = crewDocumentReferenceExtractor.extract(
                sourceDocument.getContents()
        );
        Set<UUID> candidateTargetDocumentUuids = new LinkedHashSet<>(extractedTargetDocumentUuids);
        int excludedSelfCount = removeSelfReference(
                sourceDocument.getUuid(),
                candidateTargetDocumentUuids
        );
        Map<UUID, Document> expectedTargetDocuments = findExpectedTargetDocuments(candidateTargetDocumentUuids);
        List<DocumentReference> existingReferences = documentReferenceRepository.findAllBySourceDocument(
                sourceDocument
        );

        List<DocumentReference> referencesToRemove = findReferencesToRemove(
                existingReferences,
                expectedTargetDocuments.keySet()
        );
        List<DocumentReference> referencesToAdd = createReferencesToAdd(
                sourceDocument,
                existingReferences,
                expectedTargetDocuments
        );
        applyChanges(referencesToRemove, referencesToAdd);

        int excludedMissingCount = candidateTargetDocumentUuids.size() - expectedTargetDocuments.size();
        return DocumentReferenceSyncResult.of(
                expectedTargetDocuments.keySet(),
                extractedTargetDocumentUuids.size(),
                referencesToAdd.size(),
                referencesToRemove.size(),
                excludedSelfCount,
                excludedMissingCount
        );
    }

    private int removeSelfReference(
            UUID sourceDocumentUuid,
            Set<UUID> candidateTargetDocumentUuids
    ) {
        if (candidateTargetDocumentUuids.remove(sourceDocumentUuid)) {
            return 1;
        }
        return 0;
    }

    private Map<UUID, Document> findExpectedTargetDocuments(Set<UUID> candidateTargetDocumentUuids) {
        if (candidateTargetDocumentUuids.isEmpty()) {
            return Map.of();
        }
        List<Document> targetDocuments = documentRepository.findAllByUuidIn(candidateTargetDocumentUuids);
        Map<UUID, Document> expectedTargetDocuments = new HashMap<>();
        for (Document targetDocument : targetDocuments) {
            expectedTargetDocuments.put(targetDocument.getUuid(), targetDocument);
        }
        return Map.copyOf(expectedTargetDocuments);
    }

    private List<DocumentReference> findReferencesToRemove(
            List<DocumentReference> existingReferences,
            Set<UUID> expectedTargetDocumentUuids
    ) {
        return existingReferences.stream()
                .filter(reference -> !expectedTargetDocumentUuids.contains(
                        reference.getTargetDocument().getUuid()
                ))
                .toList();
    }

    private List<DocumentReference> createReferencesToAdd(
            CrewDocument sourceDocument,
            List<DocumentReference> existingReferences,
            Map<UUID, Document> expectedTargetDocuments
    ) {
        Set<UUID> existingTargetDocumentUuids = findExistingTargetDocumentUuids(existingReferences);
        return expectedTargetDocuments.entrySet()
                .stream()
                .filter(entry -> !existingTargetDocumentUuids.contains(entry.getKey()))
                .map(entry -> DocumentReference.of(sourceDocument, entry.getValue()))
                .toList();
    }

    private Set<UUID> findExistingTargetDocumentUuids(List<DocumentReference> existingReferences) {
        Set<UUID> existingTargetDocumentUuids = new HashSet<>();
        for (DocumentReference existingReference : existingReferences) {
            existingTargetDocumentUuids.add(existingReference.getTargetDocument().getUuid());
        }
        return existingTargetDocumentUuids;
    }

    private void applyChanges(
            List<DocumentReference> referencesToRemove,
            List<DocumentReference> referencesToAdd
    ) {
        if (!referencesToRemove.isEmpty()) {
            documentReferenceRepository.deleteAll(referencesToRemove);
        }
        if (!referencesToAdd.isEmpty()) {
            documentReferenceRepository.saveAll(referencesToAdd);
        }
    }

    @Transactional
    public void deleteAllByDocument(CrewDocument crewDocument) {
        documentReferenceRepository.deleteAllBySourceDocument(crewDocument);
        documentReferenceRepository.deleteAllByTargetDocument(crewDocument);
    }
}
