package com.wooteco.wiki.organizationdocument.repository;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.document.repository.GenerationCrewOrganizationReadModel;
import com.wooteco.wiki.document.repository.GenerationCrewQueryRepository;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentOrganizationLinkRepository extends
        JpaRepository<DocumentOrganizationLink, Long>,
        GenerationCrewQueryRepository {

    Optional<DocumentOrganizationLink> findByCrewDocumentAndOrganizationDocument(
            CrewDocument crewDocument,
            OrganizationDocument organizationDocument
    );

    void deleteByCrewDocumentAndOrganizationDocument(
            CrewDocument crewDocument,
            OrganizationDocument organizationDocument
    );

    List<DocumentOrganizationLink> findAllByCrewDocument(CrewDocument crewDocument);

    @Query("SELECT documentOrganizationLink FROM DocumentOrganizationLink documentOrganizationLink " +
            "JOIN FETCH documentOrganizationLink.crewDocument " +
            "WHERE documentOrganizationLink.organizationDocument = :organizationDocument")
    List<DocumentOrganizationLink> findAllByOrganizationDocumentWithCrewDocument(
            @Param("organizationDocument") OrganizationDocument organizationDocument);

    @Override
    @Query("""
            SELECT new com.wooteco.wiki.document.repository.GenerationCrewOrganizationReadModel(
                crewDocument.title,
                crewDocument.uuid,
                organizationDocument.title
            )
            FROM DocumentOrganizationLink documentOrganizationLink
            JOIN documentOrganizationLink.crewDocument crewDocument
            JOIN documentOrganizationLink.organizationDocument organizationDocument
            WHERE crewDocument IN (
                SELECT generationLink.crewDocument
                FROM DocumentOrganizationLink generationLink
                WHERE generationLink.organizationDocument.title = :generationTitle
            )
            """)
    List<GenerationCrewOrganizationReadModel> findAllByGenerationTitle(
            @Param("generationTitle") String generationTitle
    );

    void deleteAllByCrewDocument(CrewDocument crewDocument);
}
