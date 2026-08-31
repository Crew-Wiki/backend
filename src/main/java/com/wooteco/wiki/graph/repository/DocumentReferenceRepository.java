package com.wooteco.wiki.graph.repository;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.graph.domain.DocumentReference;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentReferenceRepository extends JpaRepository<DocumentReference, Long> {

    List<DocumentReference> findAllBySourceDocument(CrewDocument sourceDocument);

    @Query("""
            SELECT new com.wooteco.wiki.graph.repository.DocumentReferenceReadModel(
                documentReference.sourceDocument.uuid,
                documentReference.targetDocument.uuid
            )
            FROM DocumentReference documentReference
            WHERE documentReference.sourceDocument IN (
                SELECT sourceGenerationLink.crewDocument
                FROM DocumentOrganizationLink sourceGenerationLink
                WHERE sourceGenerationLink.organizationDocument.title = :generationTitle
            )
            AND documentReference.targetDocument IN (
                SELECT targetGenerationLink.crewDocument
                FROM DocumentOrganizationLink targetGenerationLink
                WHERE targetGenerationLink.organizationDocument.title = :generationTitle
            )
            AND TYPE(documentReference.targetDocument) = CrewDocument
            ORDER BY documentReference.sourceDocument.uuid, documentReference.targetDocument.uuid
            """)
    List<DocumentReferenceReadModel> findAllReadModelsByGenerationTitle(
            @Param("generationTitle") String generationTitle
    );

    void deleteAllBySourceDocument(CrewDocument sourceDocument);

    void deleteAllByTargetDocument(Document targetDocument);
}
