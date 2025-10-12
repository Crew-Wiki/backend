package com.wooteco.wiki.organizationdocument.domain;

import com.wooteco.wiki.document.domain.Document;
import com.wooteco.wiki.document.domain.DocumentType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DiscriminatorValue("ORGANIZATION")
public class OrganizationDocument extends Document {

    public OrganizationDocument(final String title, final String contents, final String writer,
                                final Long documentBytes, final UUID uuid) {
        super(title, contents, writer, documentBytes, uuid);
    }

    @Override
    public DocumentType type() {
        return DocumentType.ORGANIZATION;
    }
}
