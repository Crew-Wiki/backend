package com.wooteco.wiki.document.domain;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("CREW")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class CrewDocument extends Document {

    public CrewDocument(final String title, final String contents, final String writer,
                        final Long documentBytes, final UUID uuid) {
        super(title, contents, writer, documentBytes, uuid);
    }

    @Override
    public DocumentType type() {
        return DocumentType.CREW;
    }
}
