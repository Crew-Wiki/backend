package com.wooteco.wiki.organizationdocument.repository;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentOrganizationLinkRepository extends JpaRepository<DocumentOrganizationLink, Long> {

    Optional<DocumentOrganizationLink> findByCrewDocumentAndOrganizationDocument(
            CrewDocument crewDocument,
            OrganizationDocument organizationDocument
    );

    void deleteByCrewDocumentAndOrganizationDocument(CrewDocument crewDocument,
                                                     OrganizationDocument organizationDocument);

    List<DocumentOrganizationLink> findAllByCrewDocument(CrewDocument crewDocument);

    List<DocumentOrganizationLink> findAllByOrganizationDocument(OrganizationDocument organizationDocument);

    void deleteAllByCrewDocument(CrewDocument crewDocument);
}
