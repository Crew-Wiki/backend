package com.wooteco.wiki.organizationdocument.fixture;

import com.wooteco.wiki.document.domain.CrewDocument;
import com.wooteco.wiki.organizationdocument.domain.DocumentOrganizationLink;
import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;

public class DocumentOrganizationLinkFixture {

    public static DocumentOrganizationLink create(
            CrewDocument crewDocument,
            OrganizationDocument organizationDocument
    ) {
        return new DocumentOrganizationLink(crewDocument, organizationDocument);
    }
}
