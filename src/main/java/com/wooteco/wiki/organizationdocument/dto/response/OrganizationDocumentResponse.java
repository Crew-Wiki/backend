package com.wooteco.wiki.organizationdocument.dto.response;

import com.wooteco.wiki.organizationdocument.domain.OrganizationDocument;
import com.wooteco.wiki.organizationdocument.dto.OrganizationDocumentSearchResponse;
import java.time.LocalDateTime;
import java.util.UUID;

public record OrganizationDocumentResponse(
        Long organizationDocumentId,
        UUID organizationDocumentUuid,
        String title,
        String contents,
        String writer,
        LocalDateTime generateTime
        //TODO: 로그 방식 결정 시 응답에 마지막 로그 번호 추가 요청
) {

    public OrganizationDocumentResponse(OrganizationDocument organizationDocument) {
        this(
                organizationDocument.getId(),
                organizationDocument.getUuid(),
                organizationDocument.getTitle(),
                organizationDocument.getContents(),
                organizationDocument.getWriter(),
                organizationDocument.getGenerateTime()
        );
    }

    public OrganizationDocumentSearchResponse toOrganizationDocumentSearchResponse() {
        return new OrganizationDocumentSearchResponse(
                organizationDocumentUuid,
                title
        );
    }
}
