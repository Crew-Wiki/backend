package com.wooteco.wiki.organizationdocument.repository;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DocumentOrganizationLinkRepository extends JpaRepository<DocumentOrganizationLink, Long> {

    Optional<DocumentOrganizationLink> findByCrewDocumentAndOrganizationDocument(
            CrewDocument crewDocument,
            OrganizationDocument organizationDocument
    );

    void deleteByCrewDocumentAndOrganizationDocument(CrewDocument crewDocument,
                                                     OrganizationDocument organizationDocument);

    List<DocumentOrganizationLink> findAllByCrewDocument(CrewDocument crewDocument);

    @Query("SELECT documentOrganizationLink FROM DocumentOrganizationLink documentOrganizationLink " +
            "JOIN FETCH documentOrganizationLink.crewDocument " +
            "WHERE documentOrganizationLink.organizationDocument = :organizationDocument")
    List<DocumentOrganizationLink> findAllByOrganizationDocumentWithCrewDocument(
            @Param("organizationDocument") OrganizationDocument organizationDocument);

    void deleteAllByCrewDocument(CrewDocument crewDocument);
}
